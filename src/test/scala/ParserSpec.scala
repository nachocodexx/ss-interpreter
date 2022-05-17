import mx.cinvestav.domain.{ComposeFile, Consumer, DataContainer,  Deploy, Environments, Image, Limits, ReplicaManager, Metadata, Network, Networks, Node, NodeId,Placement, PoolId, Port, Ports,  Resources, Service, Services,  Volume, Volumes,encoderComposeFile, encoderNetwork, encoderNetworks, encoderService, encoderStorageNode, encoderVolumes}
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.yaml._
import io.circe.yaml.syntax._
import mx.cinvestav.Interpreter

import scala.util.Random

class ParserSpec extends munit .CatsEffectSuite {
  final val TARGET =  "/home/nacho/Programming/Scala/interpreter/target"
//  val rabbitNodes = List(RabbitNode("148.247.201.222",5672),RabbitNode("148.247.201.222",5673))
  final val logVolume            = Volume(s"/test/logs","/app/logs")
  final val consumerLogs         = Volume("/home/jcastillo/logs/${EXPERIMENT_ID}/${EXPERIMENT_INDEX}/consumers","/app/logs")

  val mynet  = Network(name="mynet",driver=Network.NONE,external = true)





  test("Sp-5") {
    def StoragePool(
                     poolId: NodeId = NodeId("pool-0"),
                     nodes: Int = 5,
                     port: Port = Port(3000, 3000),
                     inMemory: Boolean = true,
                     basePort: Int = 4000
                   ) = {

      //      val lb  = LoadBalancing(
      //        nodeId =poolId,
      //        ports = Ports(port),
      //        networks = Networks(mynet),
      //        volumes = Volumes.empty,
      //        environments = Environments(
      //          "NODE_ID" -> poolId.value,
      //          "NODE_HOST" -> "0.0.0.0",
      //          "NODE_PORT" -> port.containerPort.toString,
      //          "MAX_RF" -> "3",
      //          "MAX_AR" -> "5",
      //          "CLOUD_ENABLED" -> "false",
      //          "IN_MEMORY" -> inMemory.toString,
      //          "REPLICATION_DAEMON" -> "false",
      //          "REPLICATION_DAEMON_DELAY" -> "1000",
      //          "DATA_REPLICATION_INTERVAL_MS" -> "10000",
      //          "DATA_REPLICATION_STRATEGY" -> "static",
      //          "SERVICE_REPLICATION_DAEMON"->"false",
      //          "SERVICE_REPLICATION_THRESHOLD"-> "0.7",
      //          "SERVICE_REPLICATION_DAEMON_DELAY" -> "10000",
      //          "UPLOAD_LOAD_BALANCER"->"UF",
      //          "DOWNLOAD_LOAD_BALANCER" -> "UF",
      //          "DEFAULT_CACHE_SIZE" -> "10",
      //          "DEFAULT_CACHE_POLICY"->"LFU",
      //          "DEFAULT_CACHE_PORT"-> "4000",
      //          "RETURN_HOSTNAME" -> "true"
      //        ),
      //      ).asInstanceOf[Service]

      //      val dcs = (0 until nodes).toList.map{ i=>
      //        val nodeId = NodeId(s"dc-$i")
      //        val nodePort = Port(basePort+i,6666)
      //        val storagePath  = Volume(s"/home/nacho/Programming/Scala/interpreter/target/sink/${nodeId.value}","/app/sink")
      //        DataContainer(
      //          nodeId   = nodeId,
      //          ports    = Ports(nodePort),
      //          networks = Networks(
      //            mynet
      //          ),
      //          volumes  = Volumes(storagePath),
      //          deploy = Deploy.of(
      //            resources = Resources.of(
      //              limits = Limits(cpus="1",memory="1G")
      //            )
      //          ),
      //          environments = Environments(
      //            "POOL_HOSTNAME" -> poolId.value,
      //            "POOL_PORT" -> port.containerPort.toString,
      //            "NODE_ID" -> nodeId.value,
      //            "NODE_HOST" -> "0.0.0.0",
      //            "NODE_PORT" -> nodePort.containerPort.toString,
      //            "POOL_ID" -> poolId.value,
      //            "CLOUD_ENABLED" -> "false",
      //            "CACHE_SIZE" -> "100",
      //            "CACHE_POLICY" -> "LFU",
      //            "IN_MEMORY" -> inMemory.toString,
      //            "STORAGE_PATH"-> storagePath.dockerPath,
      //          )
      //
      //
      //        )
      //      }.map(_.asInstanceOf[Service])
      //      ComposeFile(
      //        version = "3",
      //        services = Services((dcs:+lb):_*),
      //        volumes = Volumes.empty,
      //        networks = Networks(mynet)
      //      )
      //    }
      //    val composeFile = StoragePool()
      //    Interpreter.toSave(s"./target/output/sp5.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces4.getBytes)

    }

    //  def getLB(_sps:List[StorageNode])= {
    //    val exchangeName = "load_balancer"
    //    val  id          = NodeId("lb-0")
    //    val groupedSps   = _sps.groupBy(_.environments.values.toMap("POOL_ID"))
    ////    val logVolume = Volume("/home/jcastillo/logs","/app/logs")
    ////    val sinkVolume           = Volume(s"/home/jcastillo/sink","/app/sink")
    ////        val logVolume            = Volume(s"$TARGET/logs","/app/logs")
    ////    val sinkVolume           = Volume(s"$TARGET/sink","/app/sink")
    //    val volumes              = List(logVolume)
    //    val sps = groupedSps.map( x=>(PoolId(x._1),x._2) ).map{
    //      case (spId, storageNodes) =>
    //        SP(poolId = spId,storageNodes =storageNodes.map(_.nodeId.value),loadBalancer = "RB")
    //    }.toList
    //    val lb0Envs = LBEnvironments(nodeId = id,exchangeName = exchangeName,sns= sps)
    //    val image = Image("nachocode/load-balancer","v5")
    //    val lb0 = LB(
    //      nodeId = id,
    //      environment = lb0Envs.build(),
    //      image = image,
    //      networks = Networks(mynet),
    //      volumes =  Volumes.fromList(volumes)
    ////      deploy = Deploy(
    ////        placement = Placement(constraints = "node.role==manager"::Nil)
    ////      ).some
    //    )
    //    lb0::Nil
    //  }
    //
    //  def getDataPreparationServices(N:Int = 3,loadBalancer: LoadBalancer,basePort:Int=7000)={
    //
    //    val dataPreparationImage = Image("nachocode/data-preparation-node","v5")
    ////    val logVolume = Volume("/home/jcastillo/logs","/app/logs")
    ////    val sinkVolume           = Volume(s"/home/jcastillo/sink","/app/sink")
    //    val volumes              = List(logVolume)
    ////    val nodeIds              = (0 until N).toList.map(index => NodeId.auto("dp-"))
    //    val nodeIds              = (0 until N).toList.map(index => NodeId(s"dp-$index") )
    //    val nodeIdsWithIndex     = nodeIds.zipWithIndex
    //    //   ______________________________________________________________________________________________________
    //    nodeIdsWithIndex.map{
    //      case (nodeId,index)=>
    //        val port = basePort+index
    //        val dp0Envs = DataPreparationEnvironments(
    //          nodeId = nodeId,
    //          port   = port,
    //          host   = "0.0.0.0",
    //          loadBalancer = loadBalancer,
    //          dataPreparationNodes = nodeIdsWithIndex.map{ case (id, i) =>  DataPrepNode(id,i)},
    //          sinkFolder = ""
    //        )
    //        val dp0 = DataPreparationNode(
    //          nodeId      = nodeId,
    //          image       = dataPreparationImage,
    //          ports       = Port.singlePort(port,port),
    //          networks    = Networks(mynet),
    //          volumes     = Volumes.fromList(volumes),
    //          environment = dp0Envs.build()
    //        )
    //        dp0
    //    }
    //    //    _______________----_______________________________
    ////    val dp1Envs = dp0Envs.copy(nodeId =dp1Id,port = 7002 )
    ////    val dp1 = dp0.copy(
    ////      nodeId = dp1Id,
    ////      ports = Port.singlePort(7002,7002),
    ////      environment = dp1Envs.build()
    ////    )
    ////    //    _______________----_______________________________
    ////    val dp2Envs = dp0Envs.copy(nodeId =dp2Id,port = 7003 )
    ////    val dp2 = dp0.copy(
    ////      nodeId = dp2Id,
    ////      ports = Port.singlePort(7003,7003),
    ////      environment = dp2Envs.build()
    ////    )
    ////    val dps = List(dp0,dp1,dp2)
    ////    dps
    //  }
    //
    //  def createPool(poolId: PoolId, loadBalancer: LoadBalancer, N:Int, basePort:Int)= {
    //
    //    val storageNodeImg       = Image("nachocode/storage-node","v5")
    //    //    val sn0Vol             = Volume("sn0","/storage/", external = true)
    ////    val logVolume            = Volume(s"$TARGET/logs","/app/logs")
    ////    val sinkVolume           = Volume(s"$TARGET/sink","/app/sink")
    ////    val logVolume = Volume("/home/jcastillo/logs","/app/logs")
    ////    val sinkVolume           = Volume(s"/home/jcastillo/sink","/app/sink")
    //    //      val N = 3
    //    val totalIndexes         = (0 until N)
    //    val sns =totalIndexes.map{ i =>
    ////      val id = NodeId(s"${poolId.value}_sn-$i")
    ////      hvgjabbce6
    ////      val randomStr = Random.alphanumeric.take(10).mkString
    //      val id = NodeId.auto("sn-")
    //      val port = Port.singlePort(basePort+i,basePort+i)
    //      val sn0Envs = StorageNodeEnvironment(
    //        nodeId        = id,
    //        poolId        = poolId,
    //        logPath       =  "/app/logs",
    //        storageNodes  = Nil,
    //        loadBalancer  = loadBalancer,
    //        storagePath   = "",
    //        //      rabbitMQNodes =  ???,
    //        port          = port.values.head.hostPort,
    //        host          = "0.0.0.0",
    //        replicationStrategy = "active"
    //      )
    //      val sn0 = StorageNode(
    //        nodeId        = id,
    //        image         = storageNodeImg,
    //        ports         = port,
    //        profiles      = Nil,
    //        networks      = Networks(mynet),
    //        volumes       =  Volumes.fromList(List(logVolume)),
    //        environment   = sn0Envs.build
    //      )
    //      sn0
    //    }.toList
    //    sns
    //  }
    //
    //  def createDHT(N:Int = 3,basePort:Int=5600) ={
    //    val poolId = PoolId("ch-pool-0")
    //    val image = Image("nachocode/chord-node","v6")
    //    val M = "25"
    //    val SLOTS= "50"
    ////    val logVolume            = Volume(s"$TARGET/logs","/app/logs")
    ////    val sinkVolume           = Volume(s"$TARGET/sink","/app/sink")
    ////    val logVolume = Volume("/home/jcastillo/logs","/app/logs")
    ////    val sinkVolume           = Volume(s"/home/jcastillo/sink","/app/sink")
    //    //    val sinkVolume           = Volume(s"$TARGET/sink","/app/sink")
    //    val volumes              = List(logVolume)
    //    //
    //    val nodesIndex = (0 until N).toList
    //    val nodeIds = nodesIndex.map(x=>NodeId(s"ch-$x"))
    //    val nodes = nodeIds.zipWithIndex.map{
    //      case (chId,index)=>
    //      val port = basePort+index
    //      ChordNode(
    //        nodeId = chId,
    //        image = image,
    //        networks = Networks(mynet),
    //        volumes= Volumes.fromList(volumes),
    //        environment = Map(
    //          "NODE_ID"->chId.value,
    //          "POOL_ID"->poolId.value,
    //          "CHORD_M"->M,
    //          "CHORD_SLOTS"->SLOTS,
    //          "NODE_PORT"-> (port).toString
    //        ) ++ buildArrayEnvs(
    //          values = nodeIds.map(List(_).map(_.value)),
    //          keys = List(""),
    //          BASE_KEY = "CHORD_NODES"
    //        ),
    //        ports = Port.singlePort(port,port)
    //      )
    //
    //    }
    //    //      val nodeId = NodeId("ch-0")
    //    nodes
    //
    //    //      List(ch)
    //  }
    //
    //  test("Clientes"){
    //    val maxNumConsumers = 10
    //    val maxNumFiles     = 40
    //    val poolId          = NodeId("cache-pool-0")
    //    val maxDuration     = 300000
    //    val consumers = (0 until maxNumConsumers).map{ consumerIndex=>
    //      val consumerPort = 9000
    //      val consumerId  = NodeId(s"consumer-$consumerIndex")
    //      Consumer(
    //        nodeId = consumerId,
    //        image = Image("nachocode/experiments-suite","ex0"),
    //        ports = Port.singlePort(consumerPort+consumerIndex,consumerPort),
    //        networks = Networks(mynet),
    //        volumes = Volumes.fromList(List(consumerLogs)),
    //        environment = Map(
    //          "NODE_ID"->consumerId.value,
    //          "ROLE" -> "consumer",
    //          "LEVEL"->"CLUSTER",
    //          "MAX_DURATION_MS"->maxDuration.toString,
    //          "NUM_FILES" -> maxNumFiles.toString,
    //          "CONSUMER_INDEX" -> consumerIndex.toString,
    //          "CONSUMER_PORT"-> consumerPort.toString,
    //          "POOL_URL" -> s"http://${poolId.value}:3000/api/v7",
    //          "LOG_PATH" -> "/app/logs"
    //        )
    //      )
    //    }.toList
    //
    //    val composeFile = ComposeFile(
    //      version = "3",
    //      services = Services(consumers:_* ),
    //      volumes = Volumes.empty,
    //      networks = Networks(mynet)
    //    )
    //    Interpreter.toSave(s"./target/output/C$maxNumConsumers.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces4.getBytes)
    //  }
    //
    //  test("Chord"){
    //    val dht = createDHT(N=3)
    //    val composeFile = ComposeFile(
    //      version = "3",
    //      services = Services(services = dht:_*),
    //      volumes = Volumes.empty,
    //      networks = Networks(mynet)
    //    )
    //    Interpreter.toSave("./target/output/chord.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces4.getBytes)
    //  }
    //  test("Storage System - V5"){
    //    val poolId0  = PoolId("sp-0")
    //    val poolId1  = PoolId("sp-1")
    //    val poolId2  = PoolId("sp-2")
    //    val loadBalancer = LoadBalancer(nodeId = NodeId("lb-0"))
    //    val sp0      = createPool(poolId = poolId0,N=5,basePort = 3000,loadBalancer = loadBalancer)
    ////    val sp1      = createPool(poolId = poolId1,N=2,basePort = 4000,loadBalancer = loadBalancer)
    ////    val sp2      = createPool(poolId = poolId2,N=3,basePort = 5000,loadBalancer = loadBalancer)
    ////    val sps      = sp0 ++ sp1 ++ sp2
    //    val sps = sp0
    //    val dps      = getDataPreparationServices(N=3,loadBalancer = loadBalancer,basePort = 7100)
    //    val lb       = getLB(sps)
    //    val dht      = createDHT(N=3)
    //    //
    //    val composeFile = ComposeFile(
    //      version = "3",
    //      services = Services(services = (sps ++dps++lb):_*),
    //      volumes = Volumes.empty,
    //      networks = Networks(mynet)
    //    )
    //    Interpreter.toSave("./target/output/rms_2.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces4.getBytes)
    //  }
    //  test("array to envs"){
    ////    DATA_PREPARATION_NOES.0.node-id
    ////    DATA_PREPARATION_NOES.0.index
    //    val keys   = List("node-id","index")
    //    val values = List( List("dp-0","0"),List("dp-1","1")  )
    //    val result = buildArrayEnvs(values=values,keys=keys,BASE_KEY = "DATA_PREPARATION_NODES")
    //    val rabbit = buildRabbitNode(List(RabbitNode("0.0.0.0",1000) ,RabbitNode("0.0.0.0",2000)  ))
    //    println(rabbit)
    //    println(result)
    //
    ////    val values = ""
    //  }
    //  test("LB"){
    //    //    val dps = List(dp0,dp1,dp2)
    //
    //    val composeFile = ComposeFile(
    //      version = "3",
    //      services = Services(services = getLB(Nil):_* ),
    //      volumes = Volumes.empty,
    //      networks = Networks(mynet)
    //    )
    //    Interpreter.toSave("./target/output/lb.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces2.getBytes)
    //  }
    //  test("Data prepation"){
    //    val mynet                = Network("mynet",Network.BRIDGE,external = true)
    //    //    _______________----_______________________________
    //
    ////    val composeFile = ComposeFile(
    ////      version = "3",
    ////      services = Services(services = getDataPreparationServices() ),
    ////      volumes = Nil,
    ////      networks = Networks(mynet)
    ////    )
    ////    Interpreter.toSave("./target/output/dps_test.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces2.getBytes)
    //
    //  }
    //  test("Paxos Node"){
    //    val paxosImg = Image("nachocode/paxos-node","metadata")
    //    val mainNetwork  = Network("mynet",Network.BRIDGE,external = true)
    ////    val logVolume = Volume("/home/nacho/Programming/Scala/interpreter/target/logs","/app/logs")
    //    val logVolume = Volume("/home/jcastillo/logs","/app/logs")
    //    val poolId = PoolId("pool-xxxx")
    //    val px0Id = NodeId("px-0")
    //    val px1Id = NodeId("px-1")
    //    val px2Id = NodeId("px-2")
    //    val px3Id = NodeId("px-3")
    //    val px4Id = NodeId("px-4")
    //    val px5Id = NodeId("px-5")
    //    val px6Id = NodeId("px-6")
    //    val px7Id = NodeId("px-7")
    //    val px8Id = NodeId("px-8")
    ////    ___________________________
    ////    Test
    //    val px0 =  PaxosNode(
    //      nodeId = NodeId("px-0"),
    //      image = Image("nachocode/paxos-node","latest"),
    //      networks = Networks(Network("my_net","bridge",external = false))
    //    )
    ////
    //    val _px0 = PaxosNode(
    //      nodeId = px0Id,
    //      image = paxosImg,
    //      networks = Networks(mainNetwork),
    //      volumes = Volumes.fromList(List(logVolume)),
    ////      deploy = Deploy(
    ////        placement = Placement(constraints = "node.role==manager"::Nil)
    ////      ).some
    //    )
    //    val px1 = _px0.copy(nodeId = px1Id)
    //    val px2 = _px0.copy(nodeId = px2Id)
    //    val px3 = _px0.copy(nodeId = px3Id)
    //    val px4 = _px0.copy(nodeId = px4Id)
    //    val px5 = _px0.copy(nodeId = px5Id)
    //    val px6 = _px0.copy(nodeId = px6Id)
    //    val px7 = _px0.copy(nodeId = px7Id)
    //    val px8 = _px0.copy(nodeId = px8Id)
    //    val paxosNodes = List(_px0,px1,px2,px3,px4,px5,px6,px7,px8)
    ////  ____________________________________________________________
    //    val px0Env = PaxosNodeEnvironments(nodeId = px0Id,paxosNodes =paxosNodes,poolId = poolId )
    //    val px1Env = px0Env.copy(nodeId = px1Id)
    //    val px2Env = px0Env.copy(nodeId = px2Id)
    //    val px3Env = px0Env.copy(nodeId = px3Id)
    //    val px4Env = px0Env.copy(nodeId = px4Id)
    //    val px5Env = px0Env.copy(nodeId = px5Id)
    //    val px6Env = px0Env.copy(nodeId = px6Id)
    //    val px7Env = px0Env.copy(nodeId = px7Id)
    //    val px8Env = px0Env.copy(nodeId = px8Id)
    //    val envs = List(px0Env,px1Env,px2Env,px3Env,px4Env,px5Env,px6Env,px7Env,px8Env)
    ////   __________________________________________________
    //    val builtPaxosNodes = (paxosNodes zip  envs).map{
    //      case (node, environments) =>  node.copy(environment = environments.build())
    //    }
    ////
    //    val composeFile = ComposeFile(
    //      version = "3",
    //      services = Services(services = builtPaxosNodes:_* ),
    //      volumes = Volumes.empty,
    //      networks = Networks(mainNetwork)
    //    )
    //    Interpreter.toSave("./target/output/paxos_9.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces2.getBytes)
    ////    val
    //  }
    //  test("Bully node"){
    //    val bullyNodeImg     = Image("nachocode/bully-node","v4")
    //    val mainNetwork = Network("mynet",Network.BRIDGE,external = true)
    //    val logVolume = Volume("/home/jcastillo/logs","/app/logs")
    //    val bnId0 = NodeId("bn-0")
    //    val bnId1 = NodeId("bn-1")
    //    val bnId2 = NodeId("bn-2")
    //    val bnId3 = NodeId("bn-3")
    //    val bnId4 = NodeId("bn-4")
    //    val bnId5 = NodeId("bn-5")
    //    val bnId6 = NodeId("bn-6")
    //    val bnId7 = NodeId("bn-7")
    //    val bnId8 = NodeId("bn-8")
    //
    //    val bn0 = BullyNode(nodeId = bnId0, image = bullyNodeImg, networks = Networks(mainNetwork), volumes = Volumes.fromList(List(logVolume)))
    //    val bn1 = bn0.copy(nodeId = bnId1).updateEnvironments(Map("PRIORITY"->"1"))
    //    val bn2 = bn0.copy(nodeId = bnId2).updateEnvironments(Map("PRIORITY"->"2"))
    //    val bn3 = bn0.copy(nodeId = bnId3).updateEnvironments(Map("PRIORITY"->"3"))
    //    val bn4 = bn0.copy(nodeId = bnId4).updateEnvironments(Map("PRIORITY"->"4"))
    //    val bn5 = bn0.copy(nodeId = bnId5).updateEnvironments(Map("PRIORITY"->"5"))
    //    val bn6 = bn0.copy(nodeId = bnId6).updateEnvironments(Map("PRIORITY"->"6"))
    //    val bn7 = bn0.copy(nodeId = bnId7).updateEnvironments(Map("PRIORITY"->"7"))
    //    val bn8 = bn0.copy(nodeId = bnId8).updateEnvironments(Map("PRIORITY"->"8"))
    //    val bullyNodes_ =  List(bn0,bn1,bn2,bn3,bn4,bn5,bn6,bn7,bn8)
    ////    __________________________________________________________
    //    val poolId = PoolId("pool-xxxx")
    //
    //    val bn0Env = BullyNodeEnvironments(priority = 0, bullyNodes = bullyNodes_, nodeId = bnId0, poolId = poolId,rabbitNodes = rabbitNodes)
    //    val bn1Env = BullyNodeEnvironments(priority = 1,bullyNodes = bullyNodes_,nodeId=bnId1,poolId = poolId,rabbitNodes = rabbitNodes)
    //    val bn2Env = BullyNodeEnvironments(priority = 2, bullyNodes = bullyNodes_, nodeId=bnId2, poolId = poolId,rabbitNodes = rabbitNodes)
    //    val bn3Env = BullyNodeEnvironments(priority = 3, bullyNodes = bullyNodes_, nodeId=bnId3, poolId = poolId,rabbitNodes = rabbitNodes)
    //    val bn4Env = BullyNodeEnvironments(priority = 4, bullyNodes = bullyNodes_, nodeId=bnId4, poolId = poolId,rabbitNodes = rabbitNodes)
    //    val bn5Env = BullyNodeEnvironments(priority = 5, bullyNodes = bullyNodes_, nodeId=bnId5, poolId = poolId,rabbitNodes = rabbitNodes)
    //    val bn6Env = BullyNodeEnvironments(priority = 6, bullyNodes = bullyNodes_, nodeId=bnId6, poolId = poolId,rabbitNodes = rabbitNodes)
    //    val bn7Env = BullyNodeEnvironments(priority = 7, bullyNodes = bullyNodes_, nodeId=bnId7, poolId = poolId,rabbitNodes = rabbitNodes)
    //    val bn8Env = BullyNodeEnvironments(priority = 8, bullyNodes = bullyNodes_, nodeId=bnId8, poolId = poolId,isLeader = true,rabbitNodes = rabbitNodes)
    //    val envs = List(bn0Env,bn1Env,bn2Env,bn3Env,bn4Env,bn5Env,bn6Env,bn7Env,bn8Env)
    ////    ______________________________________________________________
    //
    //    val bullyNodes = (bullyNodes_ zip envs).map(x=>x._1.addEnvs(x._2))
    //
    //    val composeFile = ComposeFile(
    //      version = "3",
    //      services = Services(services = bullyNodes :_*),
    //      volumes = Volumes.empty,
    //      networks = Networks(mainNetwork)
    //    )
    //    Interpreter.toSave("./target/output/bully_9.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces2.getBytes)
    //
    //  }
    //  test("Basics"){
    //    import mx.cinvestav.domain.{encoderService,encoderServices}
    //    val storageNodeImage = Image(name="nachocode/storage-node",tag="v4")
    //    val networks = Networks(Network("mynet2",Network.OVERLAY,external = true))
    //    val sn0:Node = StorageNode(
    //      nodeId= NodeId("sn0"),
    //      image=storageNodeImage,
    //      ports= Ports(Port(4000,80)),
    //      networks= networks,
    //      profiles=List("test"),
    ////      storageVolume = Volume("sn0","/storage")
    //    )
    //    val sn1:Node = StorageNode(
    //      nodeId      = NodeId("sn1"),
    //      image       = storageNodeImage,
    //      ports       = Ports(Port(4001,80)),
    //      profiles    = List("debug"),
    //      networks = networks,
    ////      storageVolume = Volume("sn0","/storage")
    //    )
    //    val sn2:Node = StorageNode(
    //      nodeId      = NodeId("sn2"),
    //      image       = storageNodeImage,
    //      ports       = Ports(Port(4002,80)),
    //      profiles    = List("production"),
    //      networks = networks,
    ////      storageVolume = Volume("sn0","/storage")
    //    )
    //
    ////    val commonPort = Port.singlePort(5672,5672) ++ Port.singlePort(5673,5673)+Port.singlePort(5673,5673)
    //    val rabbit0 = RabbitMQNode(
    //      nodeId      = NodeId("rabbit-0"),
    //      image       = Image("rabbitmq","3-management"),
    //      ports       =Ports(Port(8080,15672),Port(5672,5672)),
    //      volumes     = Volumes.empty,
    ////        Volume.singleVolume("${PWD}/config/rabbit-0/","/etc/rabbitmq/"),
    //      environment = Map("RABBITMQ_CONFIG_FILE"->"/etc/rabbitmq/rabbit","RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS"->"-setcookie c6fdbfe1d74e48069327b7d82e234c68"),
    //      networks    = networks
    //    )
    //
    //
    //    val rabbit1 = rabbit0.copy(
    //      nodeId = NodeId("rabbit-1"),
    //      ports = Ports(Port(8081,15672),Port(5673,5673)),
    //      volumes = Volumes.empty
    //    )
    //    val rabbit2 = rabbit0.copy(
    //      nodeId = NodeId("rabbit-2"),
    //      ports = Ports(Port(8082,15672),Port(5674,5674)),
    //      volumes = Volumes.empty
    ////        Volume.singleVolume("${PWD}/config/rabbit-2/","/etc/rabbitmq/")
    //    )
    //    val services = Services(rabbit0,rabbit1,rabbit2)
    ////    val networks = List(Network("mynet2",Network.OVERLAY,external = true))
    ////    val volumes =services.services.flatMap(_.volumes).asJson(encoderVolumes)
    ////    println(volumes)
    //    //    val volumes = List(Volume("rabbit","target"))
    //    val composeFile = ComposeFile(version = "3",services = services,volumes = Volumes.empty,networks = networks)
    //    val yaml = composeFile.asJson(encoderComposeFile).asYaml.spaces2
    //    Interpreter.toSave("./target/output/rabbit-cluster.yml",yaml.getBytes)
    //    //    ("services"-> a.services.map(x=>x))
    ////    val moreServices = Services(services = sn2::Nil)
    ////    println(services.asJson(encoderServices).deepMerge(moreServices.asJson(encoderServices)))
    ////    val composeFile = ComposeFile(version="3",services=services)
    ////    println(services.asJson(encoderServices).asYaml.spaces2)
    ////    println(sn0.asJson(encoderService))
    //  }
  }
}
