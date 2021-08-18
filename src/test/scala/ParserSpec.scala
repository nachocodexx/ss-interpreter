import mx.cinvestav.domain.{BullyNode, BullyNodeEnviroments, ComposeFile, Deploy, Image, Metadata, Network, Networks, Node, NodeId, PaxosNode, PaxosNodeEnvironments, Placement, PoolId, Port, RabbitMQNode, Services, StorageNode, StorageNodeEnvironment, StoragePool, StoragePoolConfig, Volume, encoderComposeFile, encoderNetwork, encoderNetworks, encoderService, encoderStorageNode, encoderStoragePool, encoderVolumes}
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.yaml._
import io.circe.yaml.syntax._
import mx.cinvestav.Interpreter

class ParserSpec extends munit .CatsEffectSuite {

  test("Storage Nodes - V5"){
//    val storagePoolCfg     = StoragePoolConfig("active",2,"LZ4")
    val storageNodeImg     = Image("nachocode/storage-node","v5")
    val storagePoolNetwork = Network("mynet",Network.BRIDGE,external = true)
    val sn0Vol             = Volume("sn0","/storage/", external = true)
    val poolId = PoolId("sp-0")
    val sn0Id = NodeId("sn-0")
    val sn0Envs = StorageNodeEnvironment(
      nodeId        = sn0Id,
      poolId        = poolId,
      logPath       =  "/app/logs",
      storageNodes  = List("sn-0","sn-1","sn-2"),
      loadBalancer  = "RB",
      storagePath   = "/",
      rabbitMQNodes =  ???,
      port          = 4000,
      host = "0.0.0.0",
      replicationStrategy = "active"
    )
    val sn0 = StorageNode(
      nodeId        = sn0Id,
      image         = storageNodeImg,
      ports         = Port.singlePort(4000,80),
      profiles      = Nil,
      networks      = Networks(storagePoolNetwork),
      storageVolume = sn0Vol,
      environment = Map.empty[String,String]
    )
  }

  test("Paxos Node"){
    val paxosImg = Image("nachocode/paxos-node","metadata")
    val mainNetwork  = Network("mynet",Network.BRIDGE,external = true)
//    val logVolume = Volume("/home/nacho/Programming/Scala/interpreter/target/logs","/app/logs")
    val logVolume = Volume("/home/jcastillo/logs","/app/logs")
    val poolId = PoolId("pool-xxxx")
    val px0Id = NodeId("px-0")
    val px1Id = NodeId("px-1")
    val px2Id = NodeId("px-2")
    val px3Id = NodeId("px-3")
    val px4Id = NodeId("px-4")
    val px5Id = NodeId("px-5")
    val px6Id = NodeId("px-6")
    val px7Id = NodeId("px-7")
    val px8Id = NodeId("px-8")
//    ___________________________
//    Test
    val px0 =  PaxosNode(
      nodeId = NodeId("px-0"),
      image = Image("nachocode/paxos-node","latest"),
      networks = Networks(Network("my_net","bridge",external = false))
    )
//
    val _px0 = PaxosNode(
      nodeId = px0Id,
      image = paxosImg,
      networks = Networks(mainNetwork),
      volumes = List(logVolume),
      deploy = Deploy(
        placement = Placement(constraints = "node.role==manager"::Nil)
      ).some
    )
    val px1 = _px0.copy(nodeId = px1Id)
    val px2 = _px0.copy(nodeId = px2Id)
    val px3 = _px0.copy(nodeId = px3Id)
    val px4 = _px0.copy(nodeId = px4Id)
    val px5 = _px0.copy(nodeId = px5Id)
    val px6 = _px0.copy(nodeId = px6Id)
    val px7 = _px0.copy(nodeId = px7Id)
    val px8 = _px0.copy(nodeId = px8Id)
    val paxosNodes = List(_px0,px1,px2,px3,px4,px5,px6,px7,px8)
//  ____________________________________________________________
    val px0Env = PaxosNodeEnvironments(nodeId = px0Id,paxosNodes =paxosNodes,poolId = poolId )
    val px1Env = px0Env.copy(nodeId = px1Id)
    val px2Env = px0Env.copy(nodeId = px2Id)
    val px3Env = px0Env.copy(nodeId = px3Id)
    val px4Env = px0Env.copy(nodeId = px4Id)
    val px5Env = px0Env.copy(nodeId = px5Id)
    val px6Env = px0Env.copy(nodeId = px6Id)
    val px7Env = px0Env.copy(nodeId = px7Id)
    val px8Env = px0Env.copy(nodeId = px8Id)
    val envs = List(px0Env,px1Env,px2Env,px3Env,px4Env,px5Env,px6Env,px7Env,px8Env)
//   __________________________________________________
    val builtPaxosNodes = (paxosNodes zip  envs).map{
      case (node, environments) =>  node.copy(environment = environments.build())
    }
//
    val composeFile = ComposeFile(
      version = "3",
      services = Services(services = builtPaxosNodes ),
      volumes = Nil,
      networks = Networks(mainNetwork)
    )
    Interpreter.toSave("./target/output/paxos_9.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces2.getBytes)
//    val
  }

  test("Bully node"){
    val bullyNodeImg     = Image("nachocode/bully-node","v4")
    val mainNetwork = Network("mynet",Network.BRIDGE,external = true)
    val logVolume = Volume("/home/jcastillo/logs","/app/logs")
    val bnId0 = NodeId("bn-0")
    val bnId1 = NodeId("bn-1")
    val bnId2 = NodeId("bn-2")
    val bnId3 = NodeId("bn-3")
    val bnId4 = NodeId("bn-4")
    val bnId5 = NodeId("bn-5")
    val bnId6 = NodeId("bn-6")
    val bnId7 = NodeId("bn-7")
    val bnId8 = NodeId("bn-8")

    val bn0 = BullyNode(nodeId = bnId0, image = bullyNodeImg, networks = List(mainNetwork), volumes = List(logVolume))
    val bn1 = bn0.copy(nodeId = bnId1).updateEnvironments(Map("PRIORITY"->"1"))
    val bn2 = bn0.copy(nodeId = bnId2).updateEnvironments(Map("PRIORITY"->"2"))
    val bn3 = bn0.copy(nodeId = bnId3).updateEnvironments(Map("PRIORITY"->"3"))
    val bn4 = bn0.copy(nodeId = bnId4).updateEnvironments(Map("PRIORITY"->"4"))
    val bn5 = bn0.copy(nodeId = bnId5).updateEnvironments(Map("PRIORITY"->"5"))
    val bn6 = bn0.copy(nodeId = bnId6).updateEnvironments(Map("PRIORITY"->"6"))
    val bn7 = bn0.copy(nodeId = bnId7).updateEnvironments(Map("PRIORITY"->"7"))
    val bn8 = bn0.copy(nodeId = bnId8).updateEnvironments(Map("PRIORITY"->"8"))
    val bullyNodes_ =  List(bn0,bn1,bn2,bn3,bn4,bn5,bn6,bn7,bn8)
//    __________________________________________________________
    val poolId = PoolId("pool-xxxx")

    val bn0Env = BullyNodeEnviroments(priority = 0, bullyNodes = bullyNodes_, nodeId = bnId0, poolId = poolId)
    val bn1Env = BullyNodeEnviroments(priority = 1,bullyNodes = bullyNodes_,nodeId=bnId1,poolId = poolId)
    val bn2Env = BullyNodeEnviroments(priority = 2, bullyNodes = bullyNodes_, nodeId=bnId2, poolId = poolId)
    val bn3Env = BullyNodeEnviroments(priority = 3, bullyNodes = bullyNodes_, nodeId=bnId3, poolId = poolId)
    val bn4Env = BullyNodeEnviroments(priority = 4, bullyNodes = bullyNodes_, nodeId=bnId4, poolId = poolId)
    val bn5Env = BullyNodeEnviroments(priority = 5, bullyNodes = bullyNodes_, nodeId=bnId5, poolId = poolId)
    val bn6Env = BullyNodeEnviroments(priority = 6, bullyNodes = bullyNodes_, nodeId=bnId6, poolId = poolId)
    val bn7Env = BullyNodeEnviroments(priority = 7, bullyNodes = bullyNodes_, nodeId=bnId7, poolId = poolId)
    val bn8Env = BullyNodeEnviroments(priority = 8, bullyNodes = bullyNodes_, nodeId=bnId8, poolId = poolId,isLeader = true)
    val envs = List(bn0Env,bn1Env,bn2Env,bn3Env,bn4Env,bn5Env,bn6Env,bn7Env,bn8Env)
//    ______________________________________________________________

    val bullyNodes = (bullyNodes_ zip envs).map(x=>x._1.addEnvs(x._2))

    val composeFile = ComposeFile(
      version = "3",
      services = Services(services = bullyNodes ),
      volumes = Nil,
      networks = Networks(mainNetwork)
    )
    Interpreter.toSave("./target/output/bully_9.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces2.getBytes)

  }
  test("Storage pool"){
    val storagePoolCfg     = StoragePoolConfig("active",2,"LZ4")
    val storageNodeImg     = Image("nachocode/storage-node","v4")
    val storagePoolNetwork = Network("mynet",Network.BRIDGE,external = true)
    val sn0Vol             = Volume("sn0","/storage/", external = true)
    val sn0 = StorageNode(
      nodeId        = NodeId("sn-0"),
      image         = storageNodeImg,
      ports         = Port.singlePort(4000,80),
      profiles      = Nil,
      networks      = Networks(storagePoolNetwork),
      storageVolume = sn0Vol
    )
    val sn1 = StorageNode(
      nodeId        = NodeId("sn-1"),
      image         = storageNodeImg,
      ports         = Port.singlePort(4001,80),
      profiles      = Nil,
      networks      = Networks(storagePoolNetwork),
      storageVolume = Volume("sn0","/storage/", external = true)
    )
    val sp = StoragePool(
      poolId = PoolId("pool-xxxx"),
      storageNodes = List(sn0,sn1),
      config = storagePoolCfg
    )

    val rabbit0 = RabbitMQNode(
      nodeId=NodeId("rabbit-0"),
      image = Image("rabbitmq","3-management"),
      ports = Port(8080,15672)::Port(5672,5672)::Nil,
      volumes = Volume.singleVolume("${PWD}/config/rabbit-0/","/etc/rabbitmq/"),
      environment = Map("RABBITMQ_CONFIG_FILE"->"/etc/rabbitmq/rabbit","RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS"->"-setcookie c6fdbfe1d74e48069327b7d82e234c68"),
      networks =Networks(storagePoolNetwork)
    )

    val composeFile = ComposeFile(
      version = "3",
      services = Services(services = List(rabbit0)) + sp.toServices,
      volumes = Nil,
      networks = Networks(storagePoolNetwork)
    )
    Interpreter.toSave("./target/output/test.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces2.getBytes)
//    println(composeFile.asJson)
//    println(sn0.toJSON[StorageNode](encoderStorageNode) )
//    println(rabbit0.toJSON(encoder = encoderService)  )
//    println(sp.asJson(encoderStoragePool))
  }
//  test("Networks"){
//    val n = Network("mynet",Network.OVERLAY,external = true)
//    val ns = List(n)
//    val res = ns.asJson(encoderNetworks)
//    println(res)
//  }
  test("Basics"){
    import mx.cinvestav.domain.{encoderService,encoderServices}
    val storageNodeImage = Image(name="nachocode/storage-node",tag="v4")
    val networks = Networks(Network("mynet2",Network.OVERLAY,external = true))
    val sn0:Node = StorageNode(
      nodeId= NodeId("sn0"),
      image=storageNodeImage,
      ports=List(Port(4000,80)),
      networks= networks,
      profiles=List("test"),
      storageVolume = Volume("sn0","/storage")
    )
    val sn1:Node = StorageNode(
      nodeId      = NodeId("sn1"),
      image       = storageNodeImage,
      ports       = List(Port(4001,80)),
      profiles    = List("debug"),
      networks = networks,
      storageVolume = Volume("sn0","/storage")
    )
    val sn2:Node = StorageNode(
      nodeId      = NodeId("sn2"),
      image       = storageNodeImage,
      ports       = List(Port(4002,80)),
      profiles    = List("production"),
      networks = networks,
      storageVolume = Volume("sn0","/storage")
    )

//    val commonPort = Port.singlePort(5672,5672) ++ Port.singlePort(5673,5673)+Port.singlePort(5673,5673)
    val rabbit0 = RabbitMQNode(
      nodeId      = NodeId("rabbit-0"),
      image       = Image("rabbitmq","3-management"),
      ports       = Port(8080,15672)::Port(5672,5672)::Nil,
      volumes     = Volume.singleVolume("${PWD}/config/rabbit-0/","/etc/rabbitmq/"),
      environment = Map("RABBITMQ_CONFIG_FILE"->"/etc/rabbitmq/rabbit","RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS"->"-setcookie c6fdbfe1d74e48069327b7d82e234c68"),
      networks    = networks
    )


    val rabbit1 = rabbit0.copy(
      nodeId = NodeId("rabbit-1"),
      ports = Port(8081,15672)::Port(5673,5673)::Nil,
      volumes = Volume.singleVolume("${PWD}/config/rabbit-1/","/etc/rabbitmq/")
    )
    val rabbit2 = rabbit0.copy(
      nodeId = NodeId("rabbit-2"),
      ports = Port(8082,15672)::Port(5674,5674)::Nil,
      volumes = Volume.singleVolume("${PWD}/config/rabbit-2/","/etc/rabbitmq/")
    )
    val services = Services(services = List(rabbit0,rabbit1,rabbit2))
//    val networks = List(Network("mynet2",Network.OVERLAY,external = true))
//    val volumes =services.services.flatMap(_.volumes).asJson(encoderVolumes)
//    println(volumes)
    //    val volumes = List(Volume("rabbit","target"))
    val composeFile = ComposeFile(version = "3",services = services,volumes = Nil,networks = networks)
    val yaml = composeFile.asJson(encoderComposeFile).asYaml.spaces2
    Interpreter.toSave("./target/output/rabbit-cluster.yml",yaml.getBytes)
    //    ("services"-> a.services.map(x=>x))
//    val moreServices = Services(services = sn2::Nil)
//    println(services.asJson(encoderServices).deepMerge(moreServices.asJson(encoderServices)))
//    val composeFile = ComposeFile(version="3",services=services)
//    println(services.asJson(encoderServices).asYaml.spaces2)
//    println(sn0.asJson(encoderService))
  }

}
