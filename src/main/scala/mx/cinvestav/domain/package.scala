package mx.cinvestav
import com.sun.xml.internal.ws.wsdl.writer.document.Service
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

import javax.swing.tree.AbstractLayoutCache.NodeDimensions
//import io.circe.

package object domain {
//  _____________________________________________

  val buildRabbitNode = (rmq:List[RabbitNode])=>rmq.zipWithIndex.flatMap{
    case (node, index) =>
      val keys = List("host","port")
      val values = List(node.host,node.port.toString)
      val ENV_KEY = "RABBITMQ_NODES"
      val keyAndValues = keys zip values
      keyAndValues.map{
        case (key, value) => (s"$ENV_KEY.$index.$key",value)
      }
  }


//  _____________________________________________
  case class Metadata(data:Map[String,String]){
    def get[B](key:String,castTo:String=>B):Option[B] = data.get(key).map(castTo)
  }
  type StorageNodes = List[StorageNode]
  case class Image(name:String,tag:String){
    override def toString: String = s"$name:$tag"
  }
  case class Port(hostPort:Int,containerPort:Int){
    override def toString: String = s"$hostPort:$containerPort"
  }
  object Port{
    def singlePort(hostPort:Int,containerPort:Int) = List(Port(hostPort = hostPort,containerPort = containerPort))
  }
  type Ports = List[Port]
  case class Volume(name:String,target:String,mode:String="rw",external:Boolean=false,metadata:Metadata=Metadata(data = Map.empty[String,String])){
    override def toString: String =  s"$name:$target:$mode"
  }

  object Volume {
    def singleVolume(name:String,target:String,external:Boolean=false,mode:String="rw",metadata: Metadata=Metadata(data=Map.empty[String,String])) = List(Volume(name=name,target=target,mode=mode,
      metadata = metadata,
      external =
      external))
  }

  type Volumes = List[Volume]

  case class NodeId(value:String)
  case class ConfigBlock(subnet:String,ip_range:Option[String])

  case class Network(name:String,driver:String,external:Boolean,config:Option[List[ConfigBlock]]=None)
  object Network{
    final val OVERLAY="overlay"
    final val BRIDGE="bridge"
    final val HOST="host"
    final val MACVLAN="host"
    final val NONE ="none"
  }
  case class Networks(values:Network*)
  object Networks {
    def empty: Networks = Networks()
    def single(n:Network): Networks = Networks(n)
  }
//  type Services = List[Service]
//  type Networks = List[Network]
  case class ComposeFile(version:String, services:Services, volumes:Volumes, networks:Networks)
//
  case class Placement(constraints:List[String],max_replicas_per_node:Int=1)
  case class Deploy(placement:Placement,replicas:Int=0)

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
    def environment:Map[String,String]
    def deploy:Option[Deploy] = None
    def toJSON[A<:Service](implicit encoder:Encoder[A]):Json
  }
  case class Services(services:List[Service]){
    def +(ss:Services): Services = Services(services = services++ss.services)
  }
  trait NodeWrapper
  trait Node extends Service {
    def nodeId:NodeId
    def metadata:Metadata
//    def updateEnvironments(envs:Map[String,String]):Unit
  }
//
  case class BullyNodeEnviroments(
                                   priority:Int,
                                   bullyNodes:List[BullyNode],
                                   isLeader:Boolean=false,
                                   rabbitMQHost:String="69.0.0.2",
                                   nodeId:NodeId,
                                   poolId:PoolId,
                                   heartbeatTimeout:Int=25000,
                                   heartbeatInterval:Int=10000
                                 ) {
    def build():Map[String,String] = {
      val keys = List("node-id","priority")
      val bullyNodeKeyAndValues = bullyNodes.zipWithIndex.flatMap {
        case (x, index) =>
          val priority     = x.environment.getOrElse("PRIORITY", 0).toString
          val values       = List(x.nodeId.value,priority)
          val ENV_KEY      = "BULLY_NODES"
          val keyAndValues = (keys zip values)
          keyAndValues.map{
            case (key, value) => (s"$ENV_KEY.$index.$key",value)
          }
      }
      Map(
        "PRIORITY" -> priority.toString,
        "RABBITMQ_HOST" -> rabbitMQHost,
        "IS_LEADER" -> isLeader.toString,
        "NODE_ID" -> nodeId.value,
        "POOL_ID" -> poolId.value,
        "HEARTBEAT_TIMEOUT" -> heartbeatTimeout.toString,
        "HEARTBEAT_INTERVAL" -> heartbeatInterval.toString
      ) ++ bullyNodeKeyAndValues
    }
  }

//
  case class BullyNode(nodeId:NodeId,
                         image:Image,
                         ports:Ports = Nil ,
                         profiles:List[String]  = Nil,
                         networks: Networks,
                         volumes:Volumes = Nil,
                         depends_on:List[String] = Nil,
                         secrets:List[String] = Nil,
                         configs:List[String] = Nil,
                         environment:Map[String,String]=Map.empty[String,String],
                         metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)

   def updateEnvironments(envs: Map[String, String]): BullyNode = this.copy(environment = this.environment ++ envs)
   def addEnvs(bullyNodeEnviroments: BullyNodeEnviroments): BullyNode = this.copy(environment = this.environment ++ bullyNodeEnviroments.build())
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
        val role     = x.environment.getOrElse("ROLE", "111")
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
                       ports:Ports = Nil,
                       profiles:List[String]  = Nil,
                       networks: Networks,
                       volumes:Volumes = Nil,
                       depends_on:List[String] = Nil,
                       secrets:List[String] = Nil,
                       configs:List[String] = Nil,
                       environment:Map[String,String]=Map.empty[String,String],
                       override val deploy: Option[Deploy]= None,
                       metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)

  }
  // __________________________
  case class StorageNodeEnvironment(
                                   nodeId:NodeId,
                                   poolId:PoolId,
                                   logPath:String,
                                   loadBalancer:String,
                                   replicationFactor:Int=3,
                                   storagePath:String,
                                   storageNodes:List[String],
                                   rabbitMQNodes:List[RabbitNode]= List(RabbitNode("148.247.201.222",5672),RabbitNode("148.247.201.222",5673)),
                                   port:Int,
                                   host:String,
                                   replicationStrategy:String
                                   ){
    def  build() ={
      val STORAGE_NODES = storageNodes.zipWithIndex.map {
        case (x, index) =>
          val ENV_KEY      = "STORAGE_NODES"
          (s"$ENV_KEY.$index",x)
      }.toMap

      val RABBIT_NODES = buildRabbitNode(rabbitMQNodes)

      Map(
        "NODE_ID" -> nodeId.value,
        "POOL_ID" -> poolId.value,
        "NODE_PORT" -> port,
        "NODE_HOST" -> host,
        "REPLICATION_STRATEGY" -> replicationStrategy,
        "LOG_PATH" -> logPath,
        "LOAD_BALANCER" -> loadBalancer,
        "REPLICATION_FACTOR" -> replicationFactor,
        "STORAGE_PATH" -> storagePath,
      ) ++ STORAGE_NODES ++ RABBIT_NODES
    }
  }
  case class StorageNode(nodeId:NodeId,
                         image:Image,
                         ports:Ports ,
                         profiles:List[String]  = Nil,
                         networks: Networks,
                         storageVolume:Volume,
                         volumes:Volumes = Nil,
                         depends_on:List[String] = Nil,
                         secrets:List[String] = Nil,
                         configs:List[String] = Nil,
                         environment:Map[String,String]=Map.empty[String,String],
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
                          volumes:Volumes = Nil,
                          depends_on:List[String] = Nil,
                          secrets:List[String] = Nil,
                          configs:List[String] = Nil,
                          environment:Map[String,String]=Map.empty[String,String],
                     ) extends Service{
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
  }

  case class StoragePoolConfig(replicationStrategy:String,replicationFactor:Int,compressionAlgorithm:String)
  case class PoolId(value:String)
  case class StoragePool(
                          poolId:PoolId,
                          storageNodes: StorageNodes,
                          config:StoragePoolConfig,
                          environments:Map[String,String]=Map.empty[String,String]
                        ) extends NodeWrapper{
    def toServices: Services = {
      val sharedEnvironment = Map(
        "POOL_ID"->this.poolId.value,
        "REPLICATION_STRATEGY"->this.config.replicationStrategy,
        "REPLICATION_FACTOR"->this.config.replicationFactor.toString,
        "COMPRESSION_ALGORITHM" -> this.config.compressionAlgorithm
      ) ++ this.environments
      val storageNodes = this.storageNodes.map(s=>
        s.copy(
          environment =(s.environment++sharedEnvironment)++Map("NODE_ID"->s.nodeId.value,"STORAGE_PATH"->s.storageVolume.target  )
        )
      )
      val services     = Services(services = storageNodes)
      services
    }
  }


//  ENCODERS
  implicit val encoderStoragePool:Encoder[StoragePool] = (sp:StoragePool)=> {
    val sharedEnvironment = Map(
      "POOL_ID"->sp.poolId.value,
      "REPLICATION_STRATEGY"->sp.config.replicationStrategy,
      "REPLICATION_FACTOR"->sp.config.replicationFactor.toString,
      "COMPRESSION_ALGORITHM" -> sp.config.compressionAlgorithm
    ) ++ sp.environments
    val storageNodes = sp.storageNodes.map(s=>
      s.copy(
        environment =(s.environment++sharedEnvironment)++Map("NODE_ID"->s.nodeId.value,"STORAGE_PATH"->""   )
      )
    )
    val services     = Services(services = storageNodes)
    services.asJson(encoderServices)
  }

 implicit val encoderService:Encoder[Service] = (a: Service) => {

   val serviceListValuesMap = Map(
     "ports"-> a.ports.map(_.toString),
     "profiles" -> a.profiles,
     "depends_on" -> a.depends_on,
     "volumes" -> a.volumes.map(_.toString),
     "secrets" -> a.secrets,
     "configs" -> a.configs,
     "environment" -> a.environment.toList.map(x=>s"${x._1}=${x._2}"),
     "networks" -> a.networks.values.map(_.name),
//     "deploy" -> a.deploy
   ).filter(_._2.nonEmpty)
   val serviceSingleValueMap= Map("image"->a.image.toString,"hostname"->a.hostname)

   val json = Json.obj((a.name, serviceListValuesMap.asJson ))

   val json01 = Json.obj((a.name, serviceSingleValueMap.asJson ))
   val deploy = Json.obj((a.name,
     a.deploy.map{ d=>
     Map(("deploy"->  Map("placement" -> Map("constraints"-> d.placement.constraints)) ))
    }
     .getOrElse(Map()).asJson
   )
   )

   json.deepMerge(json01).deepMerge(deploy)
 }

  implicit val encoderStorageNode:Encoder[StorageNode] = encoderService.contramap(sn=>new Service {
    override def name: String = sn.name
    override def networks: Networks = sn.networks
    override def hostname: String = sn.hostname
    override def image: Image = sn.image
    override def ports: Ports = sn.ports
    override def profiles: List[String] = sn.profiles
    override def depends_on: List[String] = sn.depends_on
    override def volumes: Volumes = sn.volumes:+sn.storageVolume
    override def secrets: List[String] = sn.secrets
    override def configs: List[String] = sn.configs
    override def environment: Map[String, String] = sn.environment
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = sn.toJSON(encoder = encoder)
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
    ("volumes",vs.map(_.asJson(encoderVolume)).foldLeft(Json.Null)((r,v)=>r.deepMerge(v)) )
  )
  implicit val encoderNetwork:Encoder[Network] = (n:Network) => Json.obj(
    (n.name,
      Json.obj("external"->n.external.asJson,"driver"->n.driver.asJson)
      )
  )
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
