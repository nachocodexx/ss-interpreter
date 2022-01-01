package mx.cinvestav
import com.sun.xml.internal.ws.wsdl.writer.document.Service
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

import javax.swing.tree.AbstractLayoutCache.NodeDimensions
import scala.util.Random
//import io.circe.

package object domain {
//  CHORD

  case class Consumer(nodeId:NodeId,
                       image:Image,
                       ports:Ports = Ports.empty,
                       profiles:List[String]  = Nil,
                       networks: Networks,
                       volumes:Volumes = Volumes.empty,
                       depends_on:List[String] = Nil,
                       secrets:List[String] = Nil,
                       configs:List[String] = Nil,
                       environments:Environments=Environments(),
                       deploy: Option[Deploy]= None,
                       metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)

  }


  case class ChordNode(nodeId:NodeId,
                image:Image,
                ports:Ports = Ports.empty,
                profiles:List[String]  = Nil,
                networks: Networks,
                volumes:Volumes = Volumes.empty,
                depends_on:List[String] = Nil,
                secrets:List[String] = Nil,
                configs:List[String] = Nil,
                environments:Environments=Environments(),
               deploy: Option[Deploy]= None,
//                       Map[String,String]=Map.empty[String,String],
                metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)

  }
  //  LOAD BLAANCER
  case class SP(poolId: PoolId,storageNodes:List[String]=Nil,loadBalancer:String)
case class LBEnvironments(
                           nodeId:NodeId,
                           exchangeName: String="load_balancer",
                           sns:List[SP],
                           logPath:String = "/app/logs",
                           rabbitMQNodes:List[RabbitNode]= List(RabbitNode("148.247.201.222",5672),RabbitNode("148.247.201.222",5673)),
                                      ){
  def build() = {
    val RABBITMQ_NODES = buildRabbitNode(rabbitMQNodes)
    val keys = List("pool-id","load-balancer")
    val DATA_PREPARATION_NODES = sns.zipWithIndex.flatMap {
      case (x, index) =>
        val ENV_KEY      = "STORAGE_POOLS"
        val SNS_KEY = "storage-nodes"
        val sns = x.storageNodes.zipWithIndex.map(x=> (s"$ENV_KEY.$index.$SNS_KEY.${x._2}",x._1) )
        val values       = List(x.poolId.value,x.loadBalancer)
        val keyAndValues = (keys zip values)
        keyAndValues.map{
          case (key, value) => (s"$ENV_KEY.$index.$key",value)
        } ++ sns
    }
    Map(
      "NODE_ID" -> nodeId.value,
      "EXCHANGE_NAME" -> exchangeName,
      "LOG_PATH" -> logPath,
      "REPLICATION_FACTOR" -> "3",
    ) ++ RABBITMQ_NODES ++ DATA_PREPARATION_NODES
  }
}
  case class LB(nodeId:NodeId,
                         image:Image,
                         ports:Ports = Ports.empty,
                         profiles:List[String]  = Nil,
                         networks: Networks,
                         volumes:Volumes = Volumes.empty,
                         depends_on:List[String] = Nil,
                         secrets:List[String] = Nil,
                         configs:List[String] = Nil,
                         environments:Environments=Environments(),
                deploy: Option[Deploy]= None,
                metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)

  }
//  DATA PREPARATION

  case class DataPrepNode(nodeId: NodeId,index:Int)
  case class DataPreparationEnvironments(
                                        nodeId:NodeId,
//                                        poolId: PoolId,
                                        port:Int,
                                        host:String,
                                        loadBalancer: LoadBalancer,
//                                        loadBalancerExchange:String,
//                                        loadBalancerRoutingKey:String,
                                        dataPreparationNodes:List[DataPrepNode],
                                        sinkFolder:String,
                                        exchangeName:String ="data_prep",
                                        logPath:String = "/app/logs",
                                        rabbitMQNodes:List[RabbitNode]= List(RabbitNode("148.247.201.222",5672),RabbitNode("148.247.201.222",5673)),
                                        ){
    def build() = {
      val RABBITMQ_NODES = buildRabbitNode(rabbitMQNodes)
      val keys = List("node-id","index")
      val DATA_PREPARATION_NODES = dataPreparationNodes.zipWithIndex.flatMap {
        case (x, index) =>
          val values       = List(x.nodeId.value,index.toString)
          val ENV_KEY      = "DATA_PREPARATION_NODES"
          val keyAndValues = (keys zip values)
          keyAndValues.map{
            case (key, value) => (s"$ENV_KEY.$index.$key",value)
          }
      }
      Map(
        "NODE_ID" -> nodeId.value,
//        "POOL_ID" -> poolId.value,
        "NODE_PORT" -> port.toString,
        "NODE_HOST" -> host,
        "EXCHANGE_NAME" -> exchangeName,
        "LOAD_BALANCER_EXCHANGE" -> loadBalancer.exchange,
        "LOAD_BALANCER_ROUTING_KEY" -> loadBalancer.routingKey,
        "SINK_FOLDER" -> sinkFolder,
        "LOG_PATH" -> logPath
      ) ++ RABBITMQ_NODES ++ DATA_PREPARATION_NODES
    }
  }
  case class DataPreparationNode(nodeId:NodeId,
                       image:Image,
                       ports:Ports = Ports.empty,
                       profiles:List[String]  = Nil,
                       networks: Networks,
                       volumes:Volumes = Volumes.empty,
                       depends_on:List[String] = Nil,
                       secrets:List[String] = Nil,
                       configs:List[String] = Nil,
                       environments: Environments=Environments(),
//                       environment:Map[String,String]=Map.empty[String,String],
                       override val deploy: Option[Deploy]= None,
                       metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)

  }




//  _____________________________________________

  def buildArrayEnvs(values:List[List[String]],keys:List[String],BASE_KEY:String)={
    values.zipWithIndex.flatMap{
      case (vals,index)=>
        val keysWithVals = (keys zip vals)
//      println(keysWithVals)
      val data = keysWithVals.map{
//        case (key,value) => (s"$BASE_KEY.$index.$key",value)
        case (key,value) => (s"$BASE_KEY.$index${if(key.isEmpty) "" else s".$key" }",value)
      }
     data
    }.toMap
//      .toMap
  }

  def buildRabbitNode(rmq:List[RabbitNode])= {
    val ENV_KEY = "RABBITMQ_NODES"
    val keys = List("host","port")
    val values = rmq.map(x=>List(x.host,x.port.toString))
    buildArrayEnvs(values,keys,ENV_KEY)
  }
//}


//  _____________________________________________
  case class Metadata(data:Map[String,String]){
    def get[B](key:String,castTo:String=>B):Option[B] = data.get(key).map(castTo)
  }
  type StorageNodes = List[StorageNode]
  case class Image(name:String,tag:String){
    override def toString: String = s"$name:$tag"
  }
  case class Environments(values:(String,String)*)
  case class Port(hostPort:Int,containerPort:Int){override def toString: String = s"$hostPort:$containerPort"}
  case class Ports(values:Port*)
  object Ports {
    def empty: Ports = Ports()
  }
  object Port{
    def singlePort(hostPort:Int,containerPort:Int) = Ports(Port(hostPort = hostPort,containerPort = containerPort))
  }
//  type Ports = List[Port]
  case class Volume(name:String,target:String,mode:String="rw",external:Boolean=false,metadata:Metadata=Metadata(data = Map.empty[String,String])){
    override def toString: String =  s"$name:$target:$mode"
  }

  object Volume {
    def singleVolume(name:String,target:String,external:Boolean=false,mode:String="rw",metadata: Metadata=Metadata(data=Map.empty[String,String])) = List(Volume(name=name,target=target,mode=mode,
      metadata = metadata,
      external =
      external))
  }

  case class Volumes(values:Volume*)
  object Volumes {
    def empty: Volumes = Volumes()
    def fromList(vs:List[Volume]): Volumes = Volumes(vs:_*)
  }
//  type Volumes = List[Volume]

 case class NodeId(value:String)
  object NodeId {
    def auto(prefix:String,idLen:Int = 12) = NodeId(prefix+Random.alphanumeric.take(idLen).mkString)
  }
  case class ConfigBlock(subnet:String,ip_range:Option[String])

  case class Network(name:String,external:Boolean,driver:String="",config:Option[List[ConfigBlock]]=None)
  object Network{
    final val OVERLAY="overlay"
    final val BRIDGE="bridge"
    final val HOST="host"
    final val MACVLAN="host"
    final val NONE =""
  }
  case class Networks(values:Network*)
  object Networks {
    def empty: Networks = Networks()
    def single(n:Network): Networks = Networks(n)
  }
//  case class Ports(values:Ports*)
//  object  Ports {
//    def empty:Ports = Ports()
//  }
//  type Services = List[Service]
//  type Networks = List[Network]
  case class ComposeFile(version:String, services:Services, volumes:Volumes, networks:Networks)
//
  case class Placement(constraints:List[String],max_replicas_per_node:Int=1)
  object Placement {
    def empty = Option.empty[Placement]
    def of(constraints:List[String],max_replicas_per_node:Int=1) = Some(
      Placement(constraints,max_replicas_per_node)
    )
  }
  case class Limits(cpus:String,memory:String)
  object Limits{
    def of(cpus:String,memory:String) = Limits(cpus,memory).some
  }
  case class Reservations(cpus:String,memory:String)
  case class Resources(limits:Limits,reservations:Reservations = Reservations(cpus = "0",memory = "0"))
  object Resources {
    def of(limits:Limits,reservations:Reservations = Reservations(cpus = "0",memory = "0")): Option[Resources] = Some(
      Resources(limits,reservations)
    )
  }
  object RestartCondition extends Enumeration {
    type RestartCondition = String
    val onFailure = "on-failuyre"
    val none ="none"
    val any = "any"
  }
  case class RestartPolicy(condition:RestartCondition.RestartCondition,delay:String,maxAttempts:Int = 1,window:String="10s")

  case class Deploy(
                     placement:Option[Placement]=Placement.empty,
                     resources:Option[Resources]=None,
                     restartPolicy:Option[RestartPolicy]=None,
                     replicas:Option[Int]=None
                   )
  object Deploy {
    def of(
               placement:Option[Placement]=Placement.empty,
               resources:Option[Resources]=None,
               restartPolicy:Option[RestartPolicy]=None,
               replicas:Option[Int]=None
             ) =Some(Deploy(placement, resources, restartPolicy, replicas))
    def empty = Option.empty[Deploy]
//      Deploy(None,None,None,None)
  }

  trait Service {
    def name:String
    def networks:Networks
    def hostname: String
    def image:Image
    def ports:Ports
    def profiles:List[String]
    def depends_on:List[String]
    def volumes:Volumes
    def secrets:List[String]
    def configs:List[String]
    def environments:Environments
    def deploy:Option[Deploy]
    def toJSON[A<:Service](implicit encoder:Encoder[A]):Json
  }
  case class Services(services:Service*){
    def +(ss:Services): Services = Services(services = (services.toList++ss.services):_*)
  }
  trait NodeWrapper

  trait Node extends Service {
    def nodeId:NodeId
    def metadata:Metadata
  }

//
  case class BullyNodeEnvironments(
                                   priority:Int,
                                   bullyNodes:List[BullyNode],
                                   isLeader:Boolean=false,
                                   rabbitNodes: List[RabbitNode],
//                                   rabbitMQHost:String="69.0.0.2",
                                   nodeId:NodeId,
                                   poolId:PoolId,
                                   heartbeatTimeout:Int=25000,
                                   heartbeatInterval:Int=10000
                                 ) {
    def build():Map[String,String] = {
      val keys = List("node-id","priority")
      val ENV_KEY      = "BULLY_NODES"
      val values = bullyNodes.zipWithIndex.map{
        case (x, index) =>
          val priority     = x.environments.values.toMap.getOrElse("PRIORITY", 0).toString
          List(x.nodeId.value,priority)
      }
      val bullyNodeKeyAndValues = buildArrayEnvs(values,keys,ENV_KEY)
      val RABBITMQ_NODES = buildRabbitNode(rabbitNodes)

      Map(
        "PRIORITY" -> priority.toString,
//        "RABBITMQ_HOST" -> rabbitMQHost,
        "IS_LEADER" -> isLeader.toString,
        "NODE_ID" -> nodeId.value,
        "POOL_ID" -> poolId.value,
        "HEARTBEAT_TIMEOUT" -> heartbeatTimeout.toString,
        "HEARTBEAT_INTERVAL" -> heartbeatInterval.toString
      ) ++ bullyNodeKeyAndValues ++ RABBITMQ_NODES
    }
  }

//
  case class BullyNode(nodeId:NodeId,
                         image:Image,
                         ports:Ports = Ports.empty ,
                         profiles:List[String]  = Nil,
                         networks: Networks,
                         volumes:Volumes = Volumes.empty,
                         depends_on:List[String] = Nil,
                         secrets:List[String] = Nil,
                         configs:List[String] = Nil,
                       deploy: Option[Deploy]= None,
                       environments: Environments=Environments(),
                       //                         environment:Map[String,String]=Map.empty[String,String],
                         metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)

   def updateEnvironments(envs: Environments): BullyNode = {
     this.copy(
       environments =  Environments( (this.environments.values++envs.values):_* )
     )
//     this.copy(environment = (this.environment.values.toList ++ envs.toList):_*)
   }
   def addEnvs(envs: BullyNodeEnvironments): BullyNode = {
     this.copy(
       environments =  Environments( values = ( this.environments.values ++ envs.build().toList):_*)
     )
//     this.copy(environment = this.environment ++ bullyNodeEnviroments.build())
   }
  //    this.environment = this.environment++envs
}
// __________________________
  case class RabbitNode(host:String,port:Int)
case class PaxosNodeEnvironments(
                                 nodeId:NodeId,
                                 poolId:PoolId,
                                 logPath:String="/app/logs",
                                 paxosNodes:List[PaxosNode],
                                 rabbitMQHost:List[RabbitNode]= List(RabbitNode("148.247.201.222",5672),RabbitNode("148.247.201.222",5673)),
                               ) {
  def build():Map[String,String] = {
    val keys = List("node-id","role")
    val bullyNodeKeyAndValues = paxosNodes.zipWithIndex.flatMap {
      case (x, index) =>
        val role     = x.environments.values.toMap.getOrElse("ROLE", "111")
        val values       = List(x.nodeId.value,role)
        val ENV_KEY      = "PAXOS_NODES"
        val keyAndValues = (keys zip values)
        keyAndValues.map{
          case (key, value) => (s"$ENV_KEY.$index.$key",value)
        }
    }
    val rabbitMqKeyValues =  buildRabbitNode(rabbitMQHost)

    Map(
//      "RABBITMQ_NODES" -> rabbitMQHost,
      "NODE_ID" -> nodeId.value,
      "POOL_ID" -> poolId.value,
      "LOG_PATH" -> logPath
    )++ bullyNodeKeyAndValues++rabbitMqKeyValues
  }
}
  case class PaxosNode(nodeId:NodeId,
                       image:Image,
                       ports:Ports = Ports.empty,
                       profiles:List[String]  = Nil,
                       networks: Networks,
                       volumes:Volumes = Volumes.empty,
                       depends_on:List[String] = Nil,
                       secrets:List[String] = Nil,
                       configs:List[String] = Nil,
                       environments:Environments=Environments(),
//                       Map[String,String]=Map.empty[String,String],
                       override val deploy: Option[Deploy]= None,
                       metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)

  }
  // __________________________
  case class LoadBalancer(
                           nodeId:NodeId,
                           strategy:String   = "active",
                           exchange:String   = "load_balancer",
                         ){
    def routingKey:String = s"$exchange.${nodeId.value}"
  }
  // __________________________
  case class StorageNodeEnvironment(
                                   nodeId:NodeId,
                                   poolId:PoolId,
                                   logPath:String,
                                   loadBalancer:LoadBalancer,
                                   replicationFactor:Int=3,
                                   storagePath:String,
                                   storageNodes:List[String],
                                   rabbitMQNodes:List[RabbitNode]= List(RabbitNode("148.247.201.222",5672),RabbitNode("148.247.201.222",5673)),
                                   port:Int,
                                   host:String,
                                   replicationStrategy:String
                                   ){
    def  build() ={
      val RABBIT_NODES = buildRabbitNode(rabbitMQNodes)

      Map(
        "NODE_ID" -> nodeId.value,
        "POOL_ID" -> poolId.value,
        "NODE_PORT" -> port.toString,
        "NODE_HOST" -> host,
        "REPLICATION_STRATEGY" -> replicationStrategy,
        "LOG_PATH" -> logPath,
        "LOAD_BALANCER_EXCHANGE" -> loadBalancer.exchange,
        "LOAD_BALANCER_STRATEGY" -> loadBalancer.strategy,
        "LOAD_BALANCER_RK" -> loadBalancer.routingKey,
        "REPLICATION_FACTOR" -> replicationFactor.toString,
        "STORAGE_PATH" -> storagePath,
      ) ++ RABBIT_NODES
    }
  }
  case class StorageNode(nodeId:NodeId,
                         image:Image,
                         ports:Ports ,
                         profiles:List[String]  = Nil,
                         networks: Networks,
                         volumes:Volumes = Volumes.empty,
                         depends_on:List[String] = Nil,
                         secrets:List[String] = Nil,
                         configs:List[String] = Nil,
                         environments:Environments=Environments(),
                         deploy: Option[Deploy]= None,
//                         Map[String,String]=Map.empty[String,String],
                         metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
//    override def toJSON(implicit encoder: Encoder[Service]): Json = this.asInstanceOf[Service].asJson(encoder = encoder)

    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)

  }

  case class RabbitMQNode(nodeId:NodeId,
                          image:Image,
                          ports:Ports,
                          networks: Networks,
                          profiles:List[String]  = Nil,
                          volumes:Volumes = Volumes.empty,
                          depends_on:List[String] = Nil,
                          secrets:List[String] = Nil,
                          configs:List[String] = Nil,
                          environments:Environments=Environments(),
                          deploy:Option[Deploy] =None
//                          Map[String,String]=Map.empty[String,String],
                     ) extends Service{
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
  }

  case class StoragePoolConfig(replicationStrategy:String,replicationFactor:Int,compressionAlgorithm:String)
  case class PoolId(value:String)

//
case class LoadBalancing(
                         nodeId:NodeId,
                         image:Image= Image("nachocode/storage-pool","v2"),
                         ports:Ports  = Ports(),
                         profiles:List[String]  = Nil,
                         networks: Networks= Networks.empty,
                         volumes:Volumes = Volumes.empty,
                         depends_on:List[String] = Nil,
                         secrets:List[String] = Nil,
                         configs:List[String] = Nil,
                         environments:Environments=Environments(),
                         deploy:Option[Deploy] = None,
                         metadata:Metadata=Metadata(data = Map.empty[String,String]),
                       ) extends Node{
  override def hostname: String = nodeId.value
  override def name: String = nodeId.value
  override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
}
case class DataContainer(
                          nodeId:NodeId,
                       image:Image= Image("nachocode/cache-node","v2"),
                       ports:Ports  = Ports(),
                       profiles:List[String]  = Nil,
                       networks: Networks= Networks.empty,
                       volumes:Volumes = Volumes.empty,
                       depends_on:List[String] = Nil,
                       secrets:List[String] = Nil,
                       configs:List[String] = Nil,
                       environments:Environments=Environments(),
                       deploy:Option[Deploy] = None,
                       metadata:Metadata=Metadata(data = Map.empty[String,String]),
                      ) extends Node {
  override def hostname: String = nodeId.value
  override def name: String = nodeId.value
  override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
}


//  implicit val placementEncoder:Encoder[]
  implicit val deployEncoder:Encoder[Deploy] = (a:Deploy) =>{
    val placement = a.placement match {
      case Some(p) => Map(
        "constraints"-> p.constraints.asJson
      ).asJson
      case None => Json.Null
    }
    val resources = a.resources match {
      case Some(r) => Map(
            "limits"-> Map(
              "cpus"-> r.limits.cpus.asJson,
              "memory"-> r.limits.memory.asJson
            ),
            "reservations" -> Map(
              "cpus"->r.reservations.cpus.asJson,
              "memory"->r.reservations.memory.asJson
            )
      ).asJson
      case None => Json.Null
    }
    val replicas = a.replicas match {
      case Some(value) => value.asJson
      case None => Json.Null
    }
    val restart_policy = a.restartPolicy match {
      case Some(rp) => Map(
          "condition"-> rp.condition,
          "delay" -> rp.delay,
          "max_attempts"-> rp.maxAttempts.toString,
          "window" -> rp.window
      ).asJson
      case None => Json.Null
    }

    Map("deploy"->Map(
      "placement" -> placement,
      "resources" -> resources,
      "replicas" -> replicas,
      "restart_policy"->  restart_policy
    ).filter{
      case (str, json) => json != Json.Null
    }).asJson
  }
  implicit val encoderService:Encoder[Service] = (a: Service) => {

    val serviceListValuesMap = Map(
      "ports"-> a.ports.values.map(_.toString),
      "profiles" -> a.profiles,
      "depends_on" -> a.depends_on,
      "volumes" -> a.volumes.values.map(_.toString),
      "secrets" -> a.secrets,
      "configs" -> a.configs,
      "environment" -> a.environments.values.toMap.toList.map(x=>s"${x._1}=${x._2}"),
      "networks" -> a.networks.values.map(_.name),
      //     "deploy" -> a.deploy
    ).filter(_._2.nonEmpty)
    val imageAndHostname = Map("image"->a.image.toString,"hostname"->a.hostname)

    val json = Json.obj(a.name->serviceListValuesMap.asJson )

    val imageAndHostnameJson = Json.obj((a.name, imageAndHostname .asJson ))
    a.deploy match {
      case Some(value) =>
        val x = Json.obj(a.name->value.asJson(deployEncoder))
        json.deepMerge(imageAndHostnameJson).deepMerge(x)
      case None =>
        json.deepMerge(imageAndHostnameJson)
    }
  }
implicit val encoderStorageNode:Encoder[StorageNode] = encoderService.contramap(sn=>new Service {
    override def name: String = sn.name
    override def networks: Networks = sn.networks
    override def hostname: String = sn.hostname
    override def image: Image = sn.image
    override def ports: Ports = sn.ports
    override def profiles: List[String] = sn.profiles
    override def depends_on: List[String] = sn.depends_on
    override def volumes: Volumes = sn.volumes
//    :+sn.storageVolume
    override def secrets: List[String] = sn.secrets
    override def configs: List[String] = sn.configs
//    override def environments: Map[String, String] = sn.environments.values.toMap
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = sn.toJSON(encoder = encoder)
  override def environments: Environments = sn.environments

  override def deploy: Option[Deploy] = sn.deploy
})



  implicit val encoderServices:Encoder[Services] = (a:Services) => Json.obj(
    ("services"-> a.services.map(_.asJson(encoderService)).foldLeft(Json.Null)((result,service)=>result.deepMerge(service))   )
  )
  implicit val encoderServicesV2:Encoder[Services] = (a:Services) => Json.obj(
    ("services"-> a.services.map {
      case node: Node => node match {
        case node1: StorageNode => node1.asJson(encoderStorageNode)
        case _ => node.asInstanceOf[Service].asJson(encoderService)
      }
      case r: RabbitMQNode => r.asInstanceOf[Service].asJson(encoderService)
      case _ => Json.Null
    }.foldLeft(Json.Null)((result, service)=>result.deepMerge(service))   )
  )

  implicit val encoderVolume:Encoder[Volume] = (v:Volume)=> Json.obj(
    (v.name,Json.obj(("external",v.external.asJson)))
  )
  implicit val encoderVolumes:Encoder[Volumes] = (vs:Volumes)=>Json.obj(
    ("volumes",vs.values.map(_.asJson(encoderVolume)).foldLeft(Json.Null)((r,v)=>r.deepMerge(v)) )
  )
  implicit val encoderNetwork:Encoder[Network] = (n:Network) => {
    val driver = if(n.driver.isEmpty) Json.Null else n.driver.asJson
    val json = Map("driver"->driver,"external"->n.external.asJson).filter{
      case (str, json) => json != Json.Null
    }
    Json.obj(
      (n.name, json.asJson)
    )
  }
  implicit val encoderNetworks:Encoder[Networks] = (ns:Networks)=> Json.obj(
//    ("networks"-> ns.map(_.asJson(encoderNetwork)).asJson)
      ("networks"-> ns.values.map(_.asJson(encoderNetwork)).foldLeft(Json.Null)((r,n)=>r.deepMerge(n)) )
  )

  implicit val encoderComposeFile:Encoder[ComposeFile] = (cf:ComposeFile) => {
    val version = Json.obj(
      fields = ("version",cf.version.asJson)
    )
    val networks = cf.networks.asJson(encoderNetworks)
//    val volumes = Json.obj("volumes")
    val services = cf.services.asJson(encoderServicesV2)
    networks.deepMerge(services.deepMerge(version))
//      .deepMerge(networks)
  }
}
