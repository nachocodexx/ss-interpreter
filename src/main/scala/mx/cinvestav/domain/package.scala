package mx.cinvestav
import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import mx.cinvestav.domain.LoadBalancerAlgorithm.LoadBalancerAlgorithm

import java.io.{BufferedOutputStream, FileOutputStream}
import javax.swing.tree.AbstractLayoutCache.NodeDimensions
import scala.util.Random

package object domain {
object replica {
  object what {
    sealed trait What
    case class File(
                     filename:String,
                     size:Long,
                     digest:Option[Array[Byte]]
                   ) extends What
    case class Files(fs:NonEmptyList[File]) extends What
    case class Block(id:String,index:Int,guid:String,size:Long,digest:Option[Array[Byte]]) extends What
    case class Blocks(bs:NonEmptyList[Block]) extends What
  }
  //

  //
  object where {
    sealed trait Where
    case class StorageNode(id:String) extends Where
    case class StorageNodeSubset(
                                  ids:Set[String],
                                  order:Option[List[Int]]
                                ) extends Where
  }
  //
  object who {
    import mx.cinvestav.domain.replica.where.Where
    sealed trait Who
    case class Node(id:String) extends Who with Where
  }
  //
  object how {
    sealed trait How
    object ReplicationTechnique extends Enumeration {
      type ReplicationTechnique = Value
      val Active,Passive = Value
    }
    object ReplicationTransferType extends Enumeration {
      type ReplicationTransferType = Value
      val Push, Pull = Value
    }
    case class Context(replicationTechnique:ReplicationTechnique.ReplicationTechnique,
                       replicationTransferType:ReplicationTransferType.ReplicationTransferType
                      ) extends How
  }
  object when {
    sealed trait When
    case object Reactive extends When
    case class ProActive(kind:String) extends When
  }

  import mx.cinvestav.domain.replica.who.Who
  import mx.cinvestav.domain.replica.what.What
  import mx.cinvestav.domain.replica.how.How
  import mx.cinvestav.domain.replica.where.Where
  import mx.cinvestav.domain.replica.when.When


  case class Replica(
                      who:Who,
                      what:What,
                      where:Where,
                      how:How,
                      when:When
                    )
  }

  object utils {
  def toSave(outputPath:String,data:Array[Byte]): IO[Unit] = {
    val buff = new BufferedOutputStream(new FileOutputStream(outputPath)).pure[IO]
    Resource.make[IO,BufferedOutputStream](buff)(x=>IO.delay(x.close())).use(x=>IO.delay(x.write(data)))
  }
}
  //
  object ConsumerEnvs {
    def apply(
               workloadFolder:String = "/app/workloads/trace-x",
               staticExtension:String = "txt",
               sourceFolder:String = "/app/source",
               sinkFolder:String = "/app/sink",
               seed:Int= 1,
               paretoScale:Int = 1,
               paretoShape:Int =1,
               maxDownloads:Int = 100,
               consumerIndex:Int = 0,
               consumerIterations:Int = 1,
               consumerMode:String = "FROM_FILE",
               consumerPort:Int = 9000,
               consumerRate:Long = 1000,
               poolUrl:String,
               writeOnDisk:Boolean = false,
               readDebug:Boolean = false,
               appMode:String = "DOCKER"
             ) = {
      Environments(
        "WORKLOAD_FOLDER" -> workloadFolder,
        "STATIC_EXTENSION" -> staticExtension,
        "SOURCE_FOLDER" -> sourceFolder,
        "SINK_FOLDER" -> sinkFolder,
        "ROLE" -> "consumer",
        "SEED" -> seed.toString,
        "PARETO_SCALE" -> paretoScale.toString,
        "PARETO_SHAPE" -> paretoShape.toString,
        "MAX_DOWNLOADS" -> maxDownloads.toString,
//        "LEVEL" -> "",
        "CONSUMER_INDEX" -> consumerIndex.toString,
        "CONSUMER_ITERATIONS" -> consumerIterations.toString,
        "CONSUMER_MODE"->consumerMode.toString,
        "CONSUMER_PORT"->consumerPort.toString,
        "CONSUMER_RATE"->consumerRate.toString,
        "POOL_URL" -> poolUrl,
        "WRITE_ON_DISK" -> writeOnDisk.toString,
        "READ_DEBUG" ->readDebug.toString,
        "APP_MODE" -> appMode
      )
    }
  }
  case class Consumer(
                       nodeId:NodeId,
                        image:Image = Image("nachocode/experiments-suite","v2"),
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
//
  object ProducerEnvs {
    def apply(
               workloadFolder:String = "/app/workloads/trace-x",
               staticExtension:String = "txt",
               sourceFolder:String = "/app/source",
               producerMode:String = "FROM_FILE",
               producerIndex:Int = 0,
               producerRate:Long = 300,
               poolUrl:String,
               consumers:Int = 1,
               writeOnDisk:Boolean = false,
               writeDebug:Boolean = false,
               appMode:String = "DOCKER"
           ) = {
    Environments(
      "WORKLOAD_FOLDER" -> workloadFolder,
      "STATIC_EXTENSION" -> staticExtension,
      "SOURCE_FOLDER" -> sourceFolder,
      "ROLE" -> "producer",
      "PRODUCER_MODE"->producerMode,
      "PRODUCER_RATE"->producerRate.toString,
      "POOL_URL" -> poolUrl,
      "WRITE_ON_DISK" -> writeOnDisk.toString,
      "WRITE_DEBUG" ->writeDebug.toString,
      "APP_MODE" -> appMode,
      "CONSUMERS" -> consumers.toString
    )
  }
}
  case class Producer(
                       nodeId:NodeId,
                       image:Image = Image("nachocode/experiments-suite","v2"),
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


  //  _____________________________________________
  case class Metadata(data:Map[String,String]){
    def get[B](key:String,castTo:String=>B):Option[B] = data.get(key).map(castTo)
  }
  object Metadata {
    def empty = Metadata(data = Map.empty[String,String])
  }
  type StorageNodes = List[StorageNode]
  case class Image(name:String,tag:String){
    override def toString: String = s"$name:$tag"
  }
//  (String,String)*   -> Map[String,String]  -> dict() -> { }
  case class Environments(values:(String,String)*)
//

  object LoadBalancerAlgorithm extends Enumeration {
    type LoadBalancerAlgorithm = Value
    val RoundRobin   = Value("ROUND_ROBIN")
    val PseudoRandom = Value("PSEUDO_RANDOM")
    val TwoChoices   =  Value("TWO_CHOICES")
    val SortingUF    = Value("SORTING_UF")
  }
//
  case class NodeInfo(nodeId:String,port:Int,inMemory:Boolean=false)
//
  object DataReplicatorEnvs {
    def apply(
               replicationFactor:Int = 3,
               replicationMethod:String = "NONE",
               accessThreshold:Int = 1,
               uploadBalancer:LoadBalancerAlgorithm = LoadBalancerAlgorithm.SortingUF,
               downloadBalancer:LoadBalancerAlgorithm = LoadBalancerAlgorithm.SortingUF,
               daemonDelayMs:Long = 10000,
               daemonEnabled:Boolean = false,
               maxConnections:Int = 100000,
               bufferSize:Int = 65536 ,
               responseHeaderTimeoutMs:Long = 3900000,
               apiVersion:Int = 2,
               logPath:String = "/app/logs"
             ) =
      Environments(
      "API_VERSION" -> apiVersion.toString,
      "STATIC_RF" -> replicationFactor.toString,
      "REPLICATION_STRATEGY" -> replicationMethod ,
      "ACCESS_THRESHOLD" -> accessThreshold.toString,
      "LOAD_BALANCER_UPLOAD" -> uploadBalancer.toString ,
      "LOAD_BALANCER_DOWNLOAD" -> downloadBalancer.toString,
      "DAEMON_DELAY_MS" -> daemonDelayMs.toString,
      "DAEMON_ENABLED" -> daemonEnabled.toString,
      "MAX_CONNECTIONS" -> maxConnections.toString,
      "BUFFER_SIZE" -> bufferSize.toString,
      "RESPONSE_HEADER_TIMEOUT_MS" -> responseHeaderTimeoutMs.toString,
      "LOG_PATH"->logPath
      )
  }
  object SystemReplicatorEnvs {
    def apply(
               threshold:Double = 0.8,
               dataReplicatorInfo:NodeInfo = NodeInfo(nodeId = "dr-0",port = 1026),
               poolInfo:NodeInfo = NodeInfo(nodeId = "pool-0", port = 3000),
               nextPool:NodeInfo = NodeInfo(nodeId = "pool-1",port = 3001),
               monitoring:NodeInfo = NodeInfo(nodeId = "monitoring-0",port = 1027),
               cloudEnabled:Boolean = true,
               maxAr:Int = 5,
               dockerSock:String = "unix:///app/src/docker.sock",
               basePort:Int = 6666,
               baseTotalStorageCapacity:Long = 40000000000L,
               baseCachePolicy:String = "LFU",
               baseCacheSize:Int = 10,
               initNodes:Int = 0,
               hostStoragePath:String = "/test/sink",
               hostLogPath:String = "/test/logs",
               autoNodeId:Boolean = true,
               dockerNetworkName:String = "my-net",
               dockerMode:String = "LOCAL",
               daemonEnabled:Boolean = true,
               daemonDelayMs:Long = 1000,
               createNodeCoolDownMs:Long= 10000,
               maxConnections:Int = 100000,
               bufferSize:Int = 65536,
               responseHeaderTimeoutMs:Long =  10000,
               apiVersion:Int = 2,
               logPath:String = "/app/logs",
               manualStorageNodes:List[String] = List.empty[String]

             ) =
      Environments(
      "THRESHOLD" -> threshold.toString,
      "DATA_REPLICATOR_HOSTNAME" -> dataReplicatorInfo.nodeId,
      "DATA_REPLICATOR_PORT" -> dataReplicatorInfo.port.toString,
      "POOL_HOSTNAME" -> poolInfo.nodeId,
      "POOL_PORT" -> poolInfo.port.toString,
      "MONITORING_HOSTNAME" -> monitoring.nodeId,
      "MONITORING_PORT" -> monitoring.port.toString ,
      "CACHE_POOL_HOSTNAME" -> nextPool.nodeId,
      "CACHE_POOL_PORT" -> nextPool.port.toString,
      "CACHE_POOL_IN_MEMORY" -> nextPool.inMemory.toString,
      "CLOUD_ENABLED" -> cloudEnabled.toString,
      "MAX_AR" -> maxAr.toString,
      "DOCKER_SOCK" -> dockerSock,
      "BASE_PORT"->basePort.toString,
      "BASE_TOTAL_STORAGE_CAPACITY" -> baseTotalStorageCapacity.toString,
      "BASE_CACHE_POLICY"->baseCachePolicy,
      "BASE_CACHE_SIZE" -> baseCacheSize.toString,
      "INIT_NODES" -> initNodes.toString,
      "HOST_STORAGE_PATH" -> hostStoragePath,
      "HOST_LOG_PATH" -> hostLogPath,
      "API_VERSION" -> apiVersion.toString,
      "AUTO_NODE_ID" -> autoNodeId.toString,
      "DOCKER_NETWORK_NAME" -> dockerNetworkName,
      "DOCKER_MODE" -> dockerMode,
      "DAEMON_ENABLED" -> daemonEnabled.toString,
      "CREATE_NODE_COOL_DOWN_MS" -> createNodeCoolDownMs.toString,
      "DAEMON_DELAY_MS" -> daemonDelayMs.toString,
      "MAX_CONNECTIONS" -> maxConnections.toString,
      "BUFFER_SIZE" -> bufferSize.toString,
      "RESPONSE_HEADER_TIMEOUT_MS"-> responseHeaderTimeoutMs.toString,
      "LOG_PATH" -> logPath,
      "MANUAL_STORAGE_NODES" -> manualStorageNodes.mkString(",")
    )
  }
  object LoadBalancerEnvs{
    def apply(
               cloudEnabled:Boolean = true,
               returnHostname:Boolean = true,
               uploadLoadBalancer:LoadBalancerAlgorithm = LoadBalancerAlgorithm.RoundRobin,
               downloadLoadBalancer:LoadBalancerAlgorithm = LoadBalancerAlgorithm.RoundRobin,
               monitoringDelayMs:Long = 1000,
               usePublicPort:Boolean = false,
               maxConnections:Int = 100000,
               bufferSize:Int =  65536,
               responseHeaderTimeOutMs:Long = 390000
             ) = Environments(
      "CLOUD_ENABLED" -> cloudEnabled.toString,
      "RETURN_HOSTNAME" -> returnHostname.toString,
      "UPLOAD_LOAD_BALANCER" -> uploadLoadBalancer.toString,
      "DOWNLOAD_LOAD_BALANCER" -> downloadLoadBalancer.toString,
      "MONITORING_DELAY_MS" -> monitoringDelayMs.toString,
      "USE_PUBLIC_PORT" -> usePublicPort.toString,
      "MAX_CONNECTIONS" -> maxConnections.toString,
      "BUFFER_SIZE" -> bufferSize.toString,
      "RESPONSE_HEADER_TIMEOUT_MS"-> responseHeaderTimeOutMs.toString,
    )
  }
  object MonitoringEnvs {
    def apply(
               poolId:PoolId= PoolId("pool-0"),
               delayMs:Long = 5000,
               apiVersion:Int= 2,
               logPath:String = "/app/logs"
             ) = Environments(
      "POOL_ID" -> poolId.value,
      "DELAY_MS" -> delayMs.toString,
      "API_VERSION" -> apiVersion.toString,
      "LOG_PATH" -> logPath
    )
  }
  //
  case class Port(hostPort:Int,containerPort:Int){override def toString: String = s"$hostPort:$containerPort"}
  case class Ports(values:Port*)

  object Ports {
    def empty: Ports = Ports()
  }

  object Port{
    def single(hostPort:Int, dockerPort:Int) = Ports(Port(hostPort = hostPort,containerPort = dockerPort))
  }
//  type Ports = List[Port]
  case class Volume(hostPath:String, dockerPath:String, mode:String="rw", external:Boolean=false, metadata:Metadata=Metadata(data = Map.empty[String,String])){
    override def toString: String =  s"$hostPath:$dockerPath:$mode"
  }

  object Volume {
    def singleVolume(name:String,target:String,external:Boolean=false,mode:String="rw",metadata: Metadata=Metadata(data=Map.empty[String,String])) = List(Volume(hostPath=name,dockerPath=target,mode=mode,
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
  case class ComposeFile(version:String, services:Services, volumes:Volumes, networks:Networks)
  case class KubernetesDeclarativeFile(apiVersion:Int,kind:String)
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

//
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
//
  case class Services(services:Service*){
    def +(ss:Services): Services = Services(services = (services.toList++ss.services):_*)
  }

//  trait NodeWrapper

  trait Node extends Service {
    def nodeId:NodeId
    def metadata:Metadata
  }

//
  case class SystemReplicator(
                               nodeId:NodeId,
                               image:Image = Image("nachocode/system-rep","v2") ,
                               ports:Ports ,
                               networks: Networks,
                               volumes:Volumes = Volumes.empty,
                               depends_on:List[String] = Nil,
                               secrets:List[String] = Nil,
                               configs:List[String] = Nil,
                               environments:Environments=Environments(),
                               deploy: Option[Deploy]= None,
                               profiles:List[String]  = Nil,
                               metadata:Metadata=Metadata(data = Map.empty[String,String])
                             ) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
  }
  case class ReplicaManager(
                               nodeId:NodeId,
                               image:Image = Image("nachocode/load-balancer","v2") ,
                               ports:Ports ,
                               networks: Networks,
                               volumes:Volumes = Volumes.empty,
                               depends_on:List[String] = Nil,
                               secrets:List[String] = Nil,
                               configs:List[String] = Nil,
                               environments:Environments=Environments(),
                               deploy: Option[Deploy]= None,
                               profiles:List[String]  = Nil,
                               metadata:Metadata=Metadata(data = Map.empty[String,String])
                             ) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
  }
  case class DataReplicator(
                           nodeId:NodeId,
                           image:Image = Image("nachocode/data-replicator","v2") ,
                           ports:Ports ,
                           networks: Networks,
                           volumes:Volumes = Volumes.empty,
                           depends_on:List[String] = Nil,
                           secrets:List[String] = Nil,
                           configs:List[String] = Nil,
                           environments:Environments=Environments(),
                           deploy: Option[Deploy]= None,
                           profiles:List[String]  = Nil,
                           metadata:Metadata=Metadata(data = Map.empty[String,String])
                         ) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
  }
  case class Monitoring(
                             nodeId:NodeId,
                             image:Image = Image("nachocode/monitoring","v2") ,
                             ports:Ports ,
                             networks: Networks,
                             volumes:Volumes = Volumes.empty,
                             depends_on:List[String] = Nil,
                             secrets:List[String] = Nil,
                             configs:List[String] = Nil,
                             environments:Environments=Environments(),
                             deploy: Option[Deploy]= None,
                             profiles:List[String]  = Nil,
                             metadata:Metadata=Metadata(data = Map.empty[String,String])
                           ) extends Node {
    override def hostname: String = nodeId.value
    override def name: String = nodeId.value
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
  }

  object StorageNodeEnvs {
    def apply(
             poolId: PoolId = PoolId("pool-0"),
             cloudEnabled:Boolean =true,
             nextPool:NodeInfo = NodeInfo(nodeId = "pool-1", port = 3001),
             pool:NodeInfo = NodeInfo(nodeId = "pool-0",port = 3000),
             cachePolicy:String = "LFU",
             cacheSize:Int = 10,
             level:Int = 0 ,
             totalStorageCapacity:Long = 40000000000L,
             dropboxAccessToken:String = "6n9TxLVwdIIAAAAAAAAAAYV7zgDdr3XQmf9QTgfdswVNM6RFGjH",
             inMemory:Boolean = false,
//             storagePath:String ="/test/sink",
             monitoringDelayMs:Long = 1000,
             systemReplicatorInfo:NodeInfo = NodeInfo(nodeId = "sr-0",port = 1025),
             intervalMs:Long=1000,
             maxConnections:Int=10000,
             bufferSize:Int = 65536,
             responseHeaderTimeoutMs:Long= 390000,
             apiVersion:Int=2,
             delayReplicasMs:Long = 0L
             ): Environments = Environments(
      "POOL_ID" -> poolId.value,
      "CLOUD_ENABLED" -> cloudEnabled.toString ,
      "CACHE_POOL_HOSTNAME" ->  nextPool.nodeId,
      "CACHE_POOL_PORT" -> nextPool.port.toString,
      "POOL_ID" -> pool.nodeId,
      "POOL_PORT" -> pool.port.toString,
      "POOL_HOSTNAME" -> pool.nodeId,
      "CACHE_POLICY" -> cachePolicy,
      "CACHE_SIZE" -> cacheSize.toString,
      "LEVEL" -> level.toString,
      "MONITORING_DELAY_MS" -> monitoringDelayMs.toString,
      "API_VERSION" -> apiVersion.toString,
      "SERVICE_REPLICATOR_HOSTNAME" -> systemReplicatorInfo.nodeId,
      "SERVICE_REPLICATOR_PORT" -> systemReplicatorInfo.port.toString,
      "INTERVAL_MS" -> intervalMs.toString,
      "MAX_CONNECTIONS" -> maxConnections.toString,
      "BUFFER_SIZE" -> bufferSize.toString,
      "RESPONSE_HEADER_TIMEOUT_MS" -> responseHeaderTimeoutMs.toString,
      "DELAY_REPLICA_MS" -> delayReplicasMs.toString
    )
  }
  //
  case class StorageNode(
                          nodeId:NodeId,
                         image:Image = Image("nachocode/cache-node","v2"),
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
    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
  }


  case class StoragePoolConfig(replicationStrategy:String,replicationFactor:Int,compressionAlgorithm:String)
  case class PoolId(value:String)


  case class StoragePool(
                          replicaManager: ReplicaManager,
                          //                          dataReplicator: DataReplicator,
                          systemReplicator: SystemReplicator,
                          //                          monitoring: Monitoring,
                          storageNodes:List[StorageNode] = List.empty[StorageNode],
                          nextPool:Option[StoragePool]=None,
                        ){
    def toServices: Services = {
      val getFirstPort = (s:Service,defaultPort:Int) => s.ports.values.headOption.map(_.containerPort).getOrElse(defaultPort)
      nextPool match {
        case Some(_nextPool) =>
          val lbPort = getFirstPort(replicaManager,3000).toString
          val lbHostname = replicaManager.nodeId.value
          val defaultNodeInfos = Map(
              "POOL_HOSTNAME"       -> lbHostname,
              "POOL_PORT"           -> lbPort,
              "CACHE_POOL_HOSTNAME" -> _nextPool.replicaManager.nodeId.value,
              "CACHE_POOL_PORT"     -> getFirstPort(_nextPool.replicaManager,3001).toString
//                _nextPool.loadBalancer.ports.values.headOption.map(_.containerPort).getOrElse(3001).toString
            )
          val sns = storageNodes
            .map { sn =>
              val mp = defaultNodeInfos ++ sn.environments.values
              sn.copy(environments = Environments(mp.toSeq:_*))
            }
            .map(_.asInstanceOf[Service])
          val nodeInfosEnvs = Map(
              "POOL_HOSTNAME"->lbHostname,
              "POOL_PORT"->lbPort,
              "CLOUD_ENABLED" -> "false",
          )
          val ss = List(
            replicaManager
              .copy(
                environments = Environments(
                  (replicaManager.environments.values ++ Map("HAS_NEXT_POOL"->"true","CLOUD_ENABLED"->"false")).toSeq:_*
                )
              )
              .asInstanceOf[Service],
//            dataReplicator.asInstanceOf[Service],
            systemReplicator.copy(
              environments =
                Environments(
                  (systemReplicator.environments.values ++ nodeInfosEnvs):_*
                )
            ).asInstanceOf[Service],
//            monitoring.asInstanceOf[Service],
          )  ++ sns
          val value = _nextPool.toServices
          Services(ss:_*)+value
        case None =>
          val defaultNodeInfos = Map(
              "POOL_HOSTNAME"->replicaManager.nodeId.value,
              "POOL_PORT"-> replicaManager.ports.values.headOption.map(_.containerPort).getOrElse(3000).toString,
              "SERVICE_REPLICATOR_HOSTNAME" -> systemReplicator.nodeId.value,
              "SERVICE_REPLICATOR_PORT" -> systemReplicator.ports.values.head.containerPort.toString,
              "STORAGE_PATH" -> "/app/data",
              "LOG_PATH" -> "/app/logs",
          )
          val sns = storageNodes
            .map { sn =>
              val generatedEnvs = Map(
                "NODE_ID" -> sn.nodeId.value,
                "NODE_PORT" -> sn.ports.values.head.containerPort.toString,
                "TOTAL_STORAGE_CAPACITY" -> "",
                "TOTAL_MEMORY_CAPACITY" -> "",
              )
              val mp = defaultNodeInfos ++ sn.environments.values
              sn.copy(environments = Environments(mp.toSeq:_*))
            }
            .map(_.asInstanceOf[Service])
          val lbPort = replicaManager.ports.values.headOption.map(_.containerPort).getOrElse(3000).toString
          val lbHostname = replicaManager.nodeId.value

          val nodeInfosEnvs = Map(
            "POOL_HOSTNAME"->lbHostname,
            "POOL_PORT"->lbPort,
            "CLOUD_ENABLED" -> "true",
          )
          val ss = List(
            replicaManager
              .copy(
                environments = Environments(
                  (replicaManager.environments.values ++ Map("HAS_NEXT_POOL"->"false","CLOUD_ENABLED"->"true")):_*
                )
              )
              .asInstanceOf[Service],
//            dataReplicator.asInstanceOf[Service],
            systemReplicator.copy(
              environments =
                Environments(
                  (systemReplicator.environments.values ++ nodeInfosEnvs):_*
                )
            ).asInstanceOf[Service],
//            monitoring.asInstanceOf[Service],
          )  ++ sns
          Services(ss:_*)
      }
//      val defaultNodeInfos = nextPool match {
//        case Some(_nextPool) =>
//          Map(
//          "POOL_HOSTNAME"       -> loadBalancer.nodeId.value,
//          "POOL_PORT"           -> loadBalancer.ports.values.headOption.map(_.containerPort).getOrElse(3000).toString,
//          "CACHE_POOL_HOSTNAME" -> _nextPool.loadBalancer.nodeId.value,
//          "CACHE_POOL_PORT"     -> _nextPool.loadBalancer.ports.values.headOption.map(_.containerPort).getOrElse(3001).toString
//        )
//        case None =>
//          Map(
//            "POOL_HOSTNAME"->loadBalancer.nodeId.value,
//            "POOL_PORT"-> loadBalancer.ports.values.headOption.map(_.containerPort).getOrElse(3000).toString,
//          )
//      }

//      ++ storagesNodes.map(_.asInstanceOf[Service])
//      nextPoolServices match {
//        case Some(value) =>
      //        Services(ss:_*)+value
//        case None =>
      //        Services(ss:_*)
//      }
    }
  }

//
//case class LoadBalancing(
//                         nodeId:NodeId,
//                         image:Image= Image("nachocode/storage-pool","v2"),
//                         ports:Ports  = Ports(),
//                         profiles:List[String]  = Nil,
//                         networks: Networks= Networks.empty,
//                         volumes:Volumes = Volumes.empty,
//                         depends_on:List[String] = Nil,
//                         secrets:List[String] = Nil,
//                         configs:List[String] = Nil,
//                         environments:Environments=Environments(),
//                         deploy:Option[Deploy] = None,
//                         metadata:Metadata=Metadata(data = Map.empty[String,String]),
//                       ) extends Node{
//  override def hostname: String = nodeId.value
//  override def name: String = nodeId.value
//  override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
//}
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

    val envs =  Map(
      "NODE_ID" -> a.name,
      "NODE_PORT" -> a.ports.values.headOption.map(_.containerPort).getOrElse(6666).toString,
      "NODE_HOST" -> "0.0.0.0"
    ) ++ a.environments.values
    val serviceListValuesMap = Map(
      "ports"-> a.ports.values.map(_.toString),
      "profiles" -> a.profiles,
      "depends_on" -> a.depends_on,
      "volumes" -> a.volumes.values.map(_.toString),
      "secrets" -> a.secrets,
      "configs" -> a.configs,
      "environment" -> envs.toMap.toList.map(x=>s"${x._1}=${x._2}"),
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
implicit val encoderStorageNode:Encoder[StorageNode] = sn =>{
  val storagePath = sn.volumes.values.headOption.map(v=>v.dockerPath).getOrElse("/test/sink")
  val envs =  Map(
    "NODE_ID" -> sn.name,
    "NODE_PORT" -> sn.ports.values.headOption.map(_.containerPort).getOrElse(6666).toString,
    "NODE_HOST" -> "0.0.0.0",
    "STORAGE_PATH" -> storagePath
  ) ++ sn.environments.values
  val serviceListValuesMap = Map(
    "ports"-> sn.ports.values.map(_.toString),
    "profiles" -> sn.profiles,
    "depends_on" -> sn.depends_on,
    "volumes" -> sn.volumes.values.map(_.toString),
    "secrets" -> sn.secrets,
    "configs" -> sn.configs,
    "environment" -> envs.toMap.toList.map(x=>s"${x._1}=${x._2}"),
    "networks" -> sn.networks.values.map(_.name),
    //     "deploy" -> a.deploy
  ).filter(_._2.nonEmpty)
  val imageAndHostname = Map("image"->sn.image.toString,"hostname"->sn.hostname)

  val json = Json.obj(sn.name->serviceListValuesMap.asJson )

  val imageAndHostnameJson = Json.obj((sn.name, imageAndHostname .asJson ))
  sn.deploy match {
    case Some(value) =>
      val x = Json.obj(sn.name->value.asJson(deployEncoder))
      json.deepMerge(imageAndHostnameJson).deepMerge(x)
    case None =>
      json.deepMerge(imageAndHostnameJson)
  }
}
//  encoderService.contramap{
//  sn=>
//    val volumes = sn.volumes
//    val sinkEnv = volumes.values.headOption.map(v=>v.dockerPath).getOrElse(s"/test/sink")
//    println("HERE")
//    new Service {
//    override def name: String = sn.name
//    override def networks: Networks = sn.networks
//    override def hostname: String = sn.hostname
//    override def image: Image = sn.image
//    override def ports: Ports = sn.ports
//    override def profiles: List[String] = sn.profiles
//    override def depends_on: List[String] = sn.depends_on
//    override def volumes: Volumes = volumes
////      sn.volumes
//    override def secrets: List[String] = sn.secrets
//    override def configs: List[String] = sn.configs
//    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = sn.toJSON(encoder = encoder)
//    override def environments: Environments =
//      Environments(
//        (sn.environments.values ++ Map("STORAGE_PATH"->sinkEnv)):_*
//      )
//    override def deploy: Option[Deploy] = sn.deploy
//  }
//}



  implicit val encoderServices:Encoder[Services] = (a:Services) => Json.obj(
    ("services"-> a.services.map(_.asJson(encoderService)).foldLeft(Json.Null)((result,service)=>result.deepMerge(service))   )
  )
  implicit val encoderServicesV2:Encoder[Services] = (a:Services) =>
    Json.obj(
    ("services"-> a.services.map {
      case node: Node => node match {
        case node1: StorageNode => node1.asJson(encoderStorageNode)
        case _ => node.asInstanceOf[Service].asJson(encoderService)
      }
//      case r: RabbitMQNode => r.asInstanceOf[Service].asJson(encoderService)
      case _ => Json.Null
    }.foldLeft(Json.Null)((result, service)=>result.deepMerge(service))   )
  )

  implicit val encoderVolume:Encoder[Volume] = (v:Volume)=> Json.obj(
    (v.hostPath,Json.obj(("external",v.external.asJson)))
  )
  implicit val encoderVolumes:Encoder[Volumes] = (vs:Volumes)=>Json.obj(
    ("volumes",vs.values.map(_.asJson(encoderVolume)).foldLeft(Json.Null)((r,v)=>r.deepMerge(v)) )
  )
  implicit val encoderNetwork:Encoder[Network] = (n:Network) => {
    val driver = if(n.driver.isEmpty) Json.Null else n.driver.asJson
    val external = n.external
    val json = if(!external) Map(
      "driver"->driver,
      "external"->external.asJson
    ) else Map("external"->external.asJson).filter{
      case (str, json) => json != Json.Null
    }
    Json.obj(
      (n.name, json.asJson)
    )
  }
  implicit val encoderNetworks:Encoder[Networks] = (ns:Networks)=> Json.obj(
//    ("networks"-> ns.map(_.asJson(encoderNetwork)).asJson)
      ("networks"->
        ns.values.map(_.asJson(encoderNetwork)).foldLeft(Json.Null)((r,n)=>r.deepMerge(n)) )
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
//
//  case class ChordNode(nodeId:NodeId,
//                image:Image,
//                ports:Ports = Ports.empty,
//                profiles:List[String]  = Nil,
//                networks: Networks,
//                volumes:Volumes = Volumes.empty,
//                depends_on:List[String] = Nil,
//                secrets:List[String] = Nil,
//                configs:List[String] = Nil,
//                environments:Environments=Environments(),
//               deploy: Option[Deploy]= None,
////                       Map[String,String]=Map.empty[String,String],
//                metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
//    override def hostname: String = nodeId.value
//    override def name: String = nodeId.value
//    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
//
//  }
//  LOAD BLAANCER
//  case class SP(poolId: PoolId,storageNodes:List[String]=Nil,loadBalancer:String)
//case class LBEnvironments(
//                           nodeId:NodeId,
//                           exchangeName: String="load_balancer",
//                           sns:List[SP],
//                           logPath:String = "/app/logs",
//                           rabbitMQNodes:List[RabbitNode]= List(RabbitNode("148.247.201.222",5672),RabbitNode("148.247.201.222",5673)),
//                                      ){
//  def build() = {
//    val RABBITMQ_NODES = buildRabbitNode(rabbitMQNodes)
//    val keys = List("pool-id","load-balancer")
//    val DATA_PREPARATION_NODES = sns.zipWithIndex.flatMap {
//      case (x, index) =>
//        val ENV_KEY      = "STORAGE_POOLS"
//        val SNS_KEY = "storage-nodes"
//        val sns = x.storageNodes.zipWithIndex.map(x=> (s"$ENV_KEY.$index.$SNS_KEY.${x._2}",x._1) )
//        val values       = List(x.poolId.value,x.loadBalancer)
//        val keyAndValues = (keys zip values)
//        keyAndValues.map{
//          case (key, value) => (s"$ENV_KEY.$index.$key",value)
//        } ++ sns
//    }
//    Map(
//      "NODE_ID" -> nodeId.value,
//      "EXCHANGE_NAME" -> exchangeName,
//      "LOG_PATH" -> logPath,
//      "REPLICATION_FACTOR" -> "3",
//    ) ++ RABBITMQ_NODES ++ DATA_PREPARATION_NODES
//  }
//}
//  case class LB(nodeId:NodeId,
//                         image:Image,
//                         ports:Ports = Ports.empty,
//                         profiles:List[String]  = Nil,
//                         networks: Networks,
//                         volumes:Volumes = Volumes.empty,
//                         depends_on:List[String] = Nil,
//                         secrets:List[String] = Nil,
//                         configs:List[String] = Nil,
//                         environments:Environments=Environments(),
//                deploy: Option[Deploy]= None,
//                metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
//    override def hostname: String = nodeId.value
//    override def name: String = nodeId.value
//    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
//
//  }
//  DATA PREPARATION
//
//  case class DataPrepNode(nodeId: NodeId,index:Int)
//  case class DataPreparationEnvironments(
//                                        nodeId:NodeId,
////                                        poolId: PoolId,
//                                        port:Int,
//                                        host:String,
//                                        loadBalancer: LoadBalancer,
////                                        loadBalancerExchange:String,
////                                        loadBalancerRoutingKey:String,
//                                        dataPreparationNodes:List[DataPrepNode],
//                                        sinkFolder:String,
//                                        exchangeName:String ="data_prep",
//                                        logPath:String = "/app/logs",
//                                        rabbitMQNodes:List[RabbitNode]= List(RabbitNode("148.247.201.222",5672),RabbitNode("148.247.201.222",5673)),
//                                        ){
//    def build() = {
//      val RABBITMQ_NODES = buildRabbitNode(rabbitMQNodes)
//      val keys = List("node-id","index")
//      val DATA_PREPARATION_NODES = dataPreparationNodes.zipWithIndex.flatMap {
//        case (x, index) =>
//          val values       = List(x.nodeId.value,index.toString)
//          val ENV_KEY      = "DATA_PREPARATION_NODES"
//          val keyAndValues = (keys zip values)
//          keyAndValues.map{
//            case (key, value) => (s"$ENV_KEY.$index.$key",value)
//          }
//      }
//      Map(
//        "NODE_ID" -> nodeId.value,
////        "POOL_ID" -> poolId.value,
//        "NODE_PORT" -> port.toString,
//        "NODE_HOST" -> host,
//        "EXCHANGE_NAME" -> exchangeName,
//        "LOAD_BALANCER_EXCHANGE" -> loadBalancer.exchange,
//        "LOAD_BALANCER_ROUTING_KEY" -> loadBalancer.routingKey,
//        "SINK_FOLDER" -> sinkFolder,
//        "LOG_PATH" -> logPath
//      ) ++ RABBITMQ_NODES ++ DATA_PREPARATION_NODES
//    }
//  }
//  case class DataPreparationNode(nodeId:NodeId,
//                       image:Image,
//                       ports:Ports = Ports.empty,
//                       profiles:List[String]  = Nil,
//                       networks: Networks,
//                       volumes:Volumes = Volumes.empty,
//                       depends_on:List[String] = Nil,
//                       secrets:List[String] = Nil,
//                       configs:List[String] = Nil,
//                       environments: Environments=Environments(),
////                       environment:Map[String,String]=Map.empty[String,String],
//                       override val deploy: Option[Deploy]= None,
//                       metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
//    override def hostname: String = nodeId.value
//    override def name: String = nodeId.value
//    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
//
//  }
//




//  def buildArrayEnvs(values:List[List[String]],keys:List[String],BASE_KEY:String)={
//    values.zipWithIndex.flatMap{
//      case (vals,index)=>
//        val keysWithVals = (keys zip vals)
////      println(keysWithVals)
//      val data = keysWithVals.map{
////        case (key,value) => (s"$BASE_KEY.$index.$key",value)
//        case (key,value) => (s"$BASE_KEY.$index${if(key.isEmpty) "" else s".$key" }",value)
//      }
//     data
//    }.toMap
////      .toMap
//  }
//
//  def buildRabbitNode(rmq:List[RabbitNode])= {
//    val ENV_KEY = "RABBITMQ_NODES"
//    val keys = List("host","port")
//    val values = rmq.map(x=>List(x.host,x.port.toString))
//    buildArrayEnvs(values,keys,ENV_KEY)
//  }
////}

//  _____________________________________________
//  case class LoadBalancer(
//                           nodeId:NodeId,
//                           strategy:String   = "active",
//                           exchange:String   = "load_balancer",
//                         ){
//    def routingKey:String = s"$exchange.${nodeId.value}"
//  }
// __________________________
//  case class StorageNodeEnvironment(
//                                   nodeId:NodeId,
//                                   poolId:PoolId,
//                                   logPath:String,
//                                   loadBalancer:LoadBalancer,
//                                   replicationFactor:Int=3,
//                                   storagePath:String,
//                                   storageNodes:List[String],
//                                   rabbitMQNodes:List[RabbitNode]= List(RabbitNode("148.247.201.222",5672),RabbitNode("148.247.201.222",5673)),
//                                   port:Int,
//                                   host:String,
//                                   replicationStrategy:String
//                                   ){
//    def  build() ={
////      val RABBIT_NODES = buildRabbitNode(rabbitMQNodes)
//
//      Map(
//        "NODE_ID" -> nodeId.value,
//        "POOL_ID" -> poolId.value,
//        "NODE_PORT" -> port.toString,
//        "NODE_HOST" -> host,
//        "REPLICATION_STRATEGY" -> replicationStrategy,
//        "LOG_PATH" -> logPath,
//        "LOAD_BALANCER_EXCHANGE" -> loadBalancer.exchange,
//        "LOAD_BALANCER_STRATEGY" -> loadBalancer.strategy,
//        "LOAD_BALANCER_RK" -> loadBalancer.routingKey,
//        "REPLICATION_FACTOR" -> replicationFactor.toString,
//        "STORAGE_PATH" -> storagePath,
//      )
////      ++ RABBIT_NODES
//    }
//  }
// __________________________
//  case class RabbitNode(host:String,port:Int)
//case class PaxosNodeEnvironments(
//                                 nodeId:NodeId,
//                                 poolId:PoolId,
//                                 logPath:String="/app/logs",
//                                 paxosNodes:List[PaxosNode],
//                                 rabbitMQHost:List[RabbitNode]= List(RabbitNode("148.247.201.222",5672),RabbitNode("148.247.201.222",5673)),
//                               ) {
//  def build():Map[String,String] = {
//    val keys = List("node-id","role")
//    val bullyNodeKeyAndValues = paxosNodes.zipWithIndex.flatMap {
//      case (x, index) =>
//        val role     = x.environments.values.toMap.getOrElse("ROLE", "111")
//        val values       = List(x.nodeId.value,role)
//        val ENV_KEY      = "PAXOS_NODES"
//        val keyAndValues = (keys zip values)
//        keyAndValues.map{
//          case (key, value) => (s"$ENV_KEY.$index.$key",value)
//        }
//    }
//    val rabbitMqKeyValues =  buildRabbitNode(rabbitMQHost)
//
//    Map(
////      "RABBITMQ_NODES" -> rabbitMQHost,
//      "NODE_ID" -> nodeId.value,
//      "POOL_ID" -> poolId.value,
//      "LOG_PATH" -> logPath
//    )++ bullyNodeKeyAndValues++rabbitMqKeyValues
//  }
//}
//  case class PaxosNode(nodeId:NodeId,
//                       image:Image,
//                       ports:Ports = Ports.empty,
//                       profiles:List[String]  = Nil,
//                       networks: Networks,
//                       volumes:Volumes = Volumes.empty,
//                       depends_on:List[String] = Nil,
//                       secrets:List[String] = Nil,
//                       configs:List[String] = Nil,
//                       environments:Environments=Environments(),
////                       Map[String,String]=Map.empty[String,String],
//                       override val deploy: Option[Deploy]= None,
//                       metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
//    override def hostname: String = nodeId.value
//    override def name: String = nodeId.value
//    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
//
//  }
//  case class BullyNodeEnvironments(
//                                   priority:Int,
//                                   bullyNodes:List[BullyNode],
//                                   isLeader:Boolean=false,
//                                   rabbitNodes: List[RabbitNode],
////                                   rabbitMQHost:String="69.0.0.2",
//                                   nodeId:NodeId,
//                                   poolId:PoolId,
//                                   heartbeatTimeout:Int=25000,
//                                   heartbeatInterval:Int=10000
//                                 ) {
//    def build():Map[String,String] = {
//      val keys = List("node-id","priority")
//      val ENV_KEY      = "BULLY_NODES"
//      val values = bullyNodes.zipWithIndex.map{
//        case (x, index) =>
//          val priority     = x.environments.values.toMap.getOrElse("PRIORITY", 0).toString
//          List(x.nodeId.value,priority)
//      }
//      val bullyNodeKeyAndValues = buildArrayEnvs(values,keys,ENV_KEY)
//      val RABBITMQ_NODES = buildRabbitNode(rabbitNodes)
//
//      Map(
//        "PRIORITY" -> priority.toString,
////        "RABBITMQ_HOST" -> rabbitMQHost,
//        "IS_LEADER" -> isLeader.toString,
//        "NODE_ID" -> nodeId.value,
//        "POOL_ID" -> poolId.value,
//        "HEARTBEAT_TIMEOUT" -> heartbeatTimeout.toString,
//        "HEARTBEAT_INTERVAL" -> heartbeatInterval.toString
//      ) ++ bullyNodeKeyAndValues ++ RABBITMQ_NODES
//    }
//  }
//  case class BullyNode(nodeId:NodeId,
//                         image:Image,
//                         ports:Ports = Ports.empty ,
//                         profiles:List[String]  = Nil,
//                         networks: Networks,
//                         volumes:Volumes = Volumes.empty,
//                         depends_on:List[String] = Nil,
//                         secrets:List[String] = Nil,
//                         configs:List[String] = Nil,
//                       deploy: Option[Deploy]= None,
//                       environments: Environments=Environments(),
//                       //                         environment:Map[String,String]=Map.empty[String,String],
//                         metadata:Metadata=Metadata(data = Map.empty[String,String])) extends Node {
//    override def hostname: String = nodeId.value
//    override def name: String = nodeId.value
//    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
//
//   def updateEnvironments(envs: Environments): BullyNode = {
//     this.copy(
//       environments =  Environments( (this.environments.values++envs.values):_* )
//     )
////     this.copy(environment = (this.environment.values.toList ++ envs.toList):_*)
//   }
//   def addEnvs(envs: BullyNodeEnvironments): BullyNode = {
//     this.copy(
//       environments =  Environments( values = ( this.environments.values ++ envs.build().toList):_*)
//     )
////     this.copy(environment = this.environment ++ bullyNodeEnviroments.build())
//   }
//  //    this.environment = this.environment++envs
//}
// __________________________
//  case class RabbitMQNode(nodeId:NodeId,
//                          image:Image,
//                          ports:Ports,
//                          networks: Networks,
//                          profiles:List[String]  = Nil,
//                          volumes:Volumes = Volumes.empty,
//                          depends_on:List[String] = Nil,
//                          secrets:List[String] = Nil,
//                          configs:List[String] = Nil,
//                          environments:Environments=Environments(),
//                          deploy:Option[Deploy] =None
////                          Map[String,String]=Map.empty[String,String],
//                     ) extends Service{
//    override def hostname: String = nodeId.value
//    override def name: String = nodeId.value
//    override def toJSON[A <: Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
//  }
