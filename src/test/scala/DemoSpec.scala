import cats.implicits._
import mx.cinvestav.domain.{ComposeFile, Consumer, ConsumerEnvs, DataReplicator, DataReplicatorEnvs, Deploy, Environments, KubernetesDeclarativeFile, Limits, LoadBalancerEnvs, Metadata, Monitoring, MonitoringEnvs, Network, Networks, NodeId, NodeInfo, Port, Ports, Producer, ProducerEnvs, ReplicaManager, Resources, Service, Services, StorageNode, StorageNodeEnvs, StoragePool, SystemReplicator, SystemReplicatorEnvs, Volume, Volumes, encoderComposeFile, utils}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.yaml.syntax._
class DemoSpec extends munit .CatsEffectSuite {


  test("sp1") {
    val idConfig  = "sp1"
    //  Network it's equivalent to set the flag --network in the CLI.
    val myNet     = Network(
      name = "my-net",
      external = true,
      driver = Network.BRIDGE,
      config = None
    )
    //    - CLI
    //    - Docker engine API
    val logVolume        = Volume(hostPath = s"/test/logs/$idConfig", dockerPath = "/app/logs", mode ="rw")
    val dockerVolume     = Volume(hostPath = s"/var/run/docker.sock", dockerPath = "/app/src/docker.sock", mode ="rw")
//
    val replicaManager     = ReplicaManager(
      nodeId       = NodeId("rm-0"),
      ports        = Port.single(hostPort = 3000,dockerPort = 3000),
      networks     = Networks.single(myNet),
      volumes      = Volumes(logVolume),
      environments = Environments(
        "CLOUD_ENABLED" -> "true",
        "RETURN_HOSTNAME" -> "true",
        "UPLOAD_LOAD_BALANCER" -> "ROUND_ROBIN",
        "DOWNLOAD_LOAD_BALANCER" -> "ROUND_ROBIN",
        "MONITORING_DELAY_MS" -> "1000",
        "USE_PUBLIC_PORT" -> "false",
        "MAX_CONNECTIONS" -> "100000",
        "BUFFER_SIZE" -> "65536",
        "RESPONSE_HEADER_TIMEOUT_MS"-> "390000",
        "USE_PUBLIC_PORT" ->"false",
        "API_VERSION" -> "2",
        "LOG_PATH"-> "/app/logs"
      ),
      deploy       = Deploy(
        placement     = None,
        resources     = Resources(
          limits = Limits(cpus = "1", memory = "1G"),
        ).some,
        restartPolicy = None,
        replicas      =  None
      ).some
    )
//
    val systemReplicator = SystemReplicator(
      nodeId       = NodeId("sr-0"),
      ports        = Port.single(hostPort = 1025,dockerPort = 1025),
      networks     = Networks.single(myNet),
      volumes      =  Volumes(logVolume,dockerVolume),
      environments = SystemReplicatorEnvs(
        daemonDelayMs = 2000,
        initNodes = 1,
        baseCacheSize = 1,
        daemonEnabled = false
      ),
      depends_on   = List("rm-0")
    )
//
    val sp0            = StoragePool(
      replicaManager   = replicaManager,
      systemReplicator = systemReplicator,
//      DEPRECATED
//      storageNodes     = List(
//        StorageNode(
//          nodeId        = NodeId("sn-0"),
//          ports         = Port.single(40002,40002),
//          networks      = Networks.single(myNet),
//          volumes       = Volumes(
//            Volume(hostPath = "/test/logs", dockerPath = "/app/logs", mode = "rw", external = false),
//            Volume(hostPath = "/test/sink/sn-0", dockerPath = "/app/data", mode = "rw", external = false),
//          ),
//          environments = StorageNodeEnvs(),
//          deploy       = None,
//          metadata     = Metadata.empty
//        )
//      ),
      nextPool         = None
    )
    // Docker compose file
    val cf               = ComposeFile(
        version = "3",
        services = sp0.toServices,
        volumes = Volumes.empty,
        networks = Networks(myNet)
    )
   // K8s
//   val k8s = KubernetesDeclarativeFile(apiVersion = 3, kind = "Deployment")
    utils.toSave(s"./target/output/$idConfig.yml",cf.asJson(encoderComposeFile).asYaml.spaces4.getBytes)
  }

  test("none-sp1 -> none-sp1 -> cloud") {
//
    val idConfig          = "none-sp1_none-sp1_cloud"
//   NETWORK
    val myNet             = Network(
      name = "my-net",
      external = true,
      driver = Network.BRIDGE,
      config = None
    )
//   VOLUMES
    val logVolume         = Volume(hostPath = s"/test/logs/$idConfig", dockerPath = "/app/logs", mode ="rw")
    val dockerVolume      = Volume(hostPath = s"/var/run/docker.sock", dockerPath = "/app/src/docker.sock", mode ="rw")
//  NODES
    val loadBalancer      = ReplicaManager(
      nodeId       = NodeId("pool-0"),
      ports        = Port.single(hostPort = 3000,dockerPort = 3000),
      networks     = Networks.single(myNet),
      volumes      =  Volumes(logVolume),
      environments = Environments(
        "CLOUD_ENABLED" -> "false",
        "RETURN_HOSTNAME" -> "true",
        "UPLOAD_LOAD_BALANCER" -> "ROUND_ROBIN",
        "DOWNLOAD_LOAD_BALANCER" -> "ROUND_ROBIN",
        "MONITORING_DELAY_MS" -> "1000",
        "USE_PUBLIC_PORT" -> "false",
        "MAX_CONNECTIONS" -> "100000",
        "BUFFER_SIZE" -> "65536",
        "RESPONSE_HEADER_TIMEOUT_MS"-> "390000",
        "USE_PUBLIC_PORT" ->"false"
      ),
      deploy       = Deploy(
        placement     = None,
        resources     = Resources(
          limits = Limits(cpus = "1", memory = "1G"),
        ).some,
        restartPolicy = None,
        replicas      =  None
      ).some
    )

    val loadBalancer1     = loadBalancer.copy(
      nodeId = NodeId("pool-1"),
      ports = Port.single(hostPort = 3001,dockerPort = 3001),
      environments= LoadBalancerEnvs()
    )
    //
    val dataReplicator    = DataReplicator(
      nodeId   = NodeId("dr-0"),
      ports    = Port.single(hostPort = 1026,dockerPort = 1026),
      networks = Networks.single(myNet),
      volumes  =  Volumes(logVolume),
      environments = DataReplicatorEnvs()
    )
    val dataReplicator1   = dataReplicator.copy(
      nodeId = NodeId("dr-1"),
      ports  = Port.single(hostPort = 2026,dockerPort = 2026)
    )
//
    val systemReplicator  = SystemReplicator(
      nodeId       = NodeId("sr-0"),
      ports        = Port.single(hostPort = 1025,dockerPort = 1025),
      networks     = Networks.single(myNet),
      volumes      =  Volumes(logVolume,dockerVolume),
      environments = SystemReplicatorEnvs(
        daemonDelayMs = 2000,
        initNodes = 1,
        baseCacheSize = 1,
        daemonEnabled = true,
        threshold = 0.8
      ),
      depends_on   = List("monitoring-0","dr-0","pool-0")
    )
    val systemReplicator1 = systemReplicator.copy(
      nodeId =  NodeId("sr-1"),
      ports = Port.single(hostPort = 2025,dockerPort = 2025),
      environments = SystemReplicatorEnvs(
        daemonDelayMs = 2000,
        initNodes = 1,
        baseCacheSize = 2,
        daemonEnabled = false,
//        poolInfo = NodeInfo()
      )
    )
//
    val monitoring        = Monitoring(
      nodeId = NodeId("monitoring-0"),
      ports = Port.single(hostPort = 1027,dockerPort = 1027),
      networks = Networks.single(myNet),
      volumes =  Volumes(logVolume),
      environments = MonitoringEnvs()
    )
    val monitoring1       = monitoring.copy(nodeId = NodeId("monitoring-1"),ports = Port.single(hostPort = 2027,dockerPort = 2027))
//
    val sp1               = StoragePool(
      replicaManager = loadBalancer1,
      systemReplicator =systemReplicator1,
      nextPool = None
    )

    val sp0               = StoragePool(
      replicaManager     = loadBalancer,
      systemReplicator = systemReplicator,
      nextPool         = Some(sp1)
    )
//  _______________________________________________
    val cf                = ComposeFile(
      version = "3",
      services = sp0.toServices,
      volumes = Volumes.empty,
      networks = Networks(myNet)
    )
    utils
      .toSave(s"./target/output/$idConfig.yml",cf.asJson(encoderComposeFile).asYaml.spaces4.getBytes)
  }

  test("static-sp5 - RF = 3") {
    val idConfig  = "static-sp5"
    //  Network it's equivalent to set the flag --network in the CLI.
    //    - CLI
    //    - Docker engine API
    val myNet             = Network(
      name = "my-net",
      external = true,
      driver = Network.BRIDGE,
      config = None
    )
    //  /test/logs/<idConfig>
    val logVolume         = Volume(hostPath = s"/test/logs/$idConfig", dockerPath = "/app/logs", mode ="rw")
    val dockerVolume      = Volume(hostPath = s"/var/run/docker.sock", dockerPath = "/app/src/docker.sock", mode ="rw")
//
    val loadBalancer      = ReplicaManager(
      nodeId       = NodeId("pool-0"),
      ports        = Port.single(hostPort = 3000,dockerPort = 3000),
      networks     = Networks.single(myNet),
      volumes      =  Volumes(logVolume),
      environments = Environments(
        "CLOUD_ENABLED" -> "false",
        "RETURN_HOSTNAME" -> "true",
        "UPLOAD_LOAD_BALANCER" -> "ROUND_ROBIN",
        "DOWNLOAD_LOAD_BALANCER" -> "ROUND_ROBIN",
        "MONITORING_DELAY_MS" -> "1000",
        "USE_PUBLIC_PORT" -> "false",
        "MAX_CONNECTIONS" -> "100000",
        "BUFFER_SIZE" -> "65536",
        "RESPONSE_HEADER_TIMEOUT_MS"-> "390000",
        "USE_PUBLIC_PORT" ->"false"
      ),
      deploy       = Deploy(
        placement     = None,
        resources     = Resources(
          limits = Limits(cpus = "1", memory = "1G"),
        ).some,
        restartPolicy = None,
        replicas      =  None
      ).some
    )
//
    val dataReplicator    = DataReplicator(
      nodeId   = NodeId("dr-0"),
      ports    = Port.single(hostPort = 1026,dockerPort = 1026),
      networks = Networks.single(myNet),
      volumes  =  Volumes(logVolume),
      environments = DataReplicatorEnvs(
        daemonEnabled     = true,
        daemonDelayMs     = 5000,
        replicationMethod = "STATIC",
        replicationFactor = 3,
        accessThreshold = 20
      )
    )
  //
    val systemReplicator  = SystemReplicator(
      nodeId       = NodeId("sr-0"),
      ports        = Port.single(hostPort = 1025,dockerPort = 1025),
      networks     = Networks.single(myNet),
      volumes      =  Volumes(logVolume,dockerVolume),
      environments = SystemReplicatorEnvs(
        daemonDelayMs = 2000,
        initNodes = 5,
        baseCacheSize = 10,
        daemonEnabled = false
      ),
      depends_on   = List("monitoring-0","dr-0","pool-0")
    )

    val monitoring        = Monitoring(
      nodeId       = NodeId("monitoring-0"),
      ports        = Port.single(hostPort = 1027,dockerPort = 1027),
      networks     = Networks.single(myNet),
      volumes      =  Volumes(logVolume),
      environments = MonitoringEnvs()
    )
    //
    val sp0               = StoragePool(
      replicaManager     = loadBalancer,
      systemReplicator = systemReplicator,
    )
    val cf                = ComposeFile(
      version = "3",
      services = sp0.toServices,
      volumes = Volumes.empty,
      networks = Networks(myNet)
    )
//
    utils
      .toSave(s"./target/output/$idConfig.yml",cf.asJson(encoderComposeFile).asYaml.spaces4.getBytes)
  }

  test("Consumer and producers"){
    val idConfig     = "producer_consumers"
    val myNet        = Network(
      name = "my-net",
      external = true,
      driver = Network.BRIDGE,
      config = None
    )
    val logVolume    = Volume(hostPath = s"/test/logs/$idConfig", dockerPath = "/app/logs", mode ="rw")
    val workloadVol  = Volume(hostPath = "/test/workloads", dockerPath = "/app/workloads", mode ="rw")
    val sourceVolume = Volume(hostPath = s"/test/source", dockerPath = "/app/source", mode ="rw")
    val sinkVolume   = Volume(hostPath = s"/test/source", dockerPath = "/app/sink", mode ="rw")
//
    val p0           = Producer(
      nodeId = NodeId("producer-0"),
      networks = Networks.single(myNet),
      volumes = Volumes(
        logVolume,
        workloadVol,
        sourceVolume,
        sinkVolume
      ),
      environments = ProducerEnvs(
        poolUrl   = "http://pool-0:3000/api/v2",
        consumers = 2
      ),
      depends_on = List("consumer-0","consumer-1")
    )
    val c0           = Consumer(
      nodeId = NodeId("consumer-0"),
      ports = Port.single(hostPort = 9000,dockerPort = 9000),
      networks = Networks.single(myNet),
      volumes = Volumes(
        logVolume,
        workloadVol,
        sourceVolume,
        sinkVolume
      ),
      environments = ConsumerEnvs(
        poolUrl = "http://pool-0:3000/api/v2"
      ),
    )
    val c1           = c0.copy(
      nodeId = NodeId("consumer-1"),
      ports = Port.single(hostPort = 9001,dockerPort = 9000)
    )

    val cf           = ComposeFile(
      version = "3",
      services = Services(
        Seq(c0, c1,p0).map(_.asInstanceOf[Service]):_*
      ),
      volumes = Volumes.empty,
      networks = Networks(myNet)
    )
    utils.toSave(s"./target/output/$idConfig.yml",cf.asJson(encoderComposeFile).asYaml.spaces4.getBytes)

  }

  test("Consumers - N") {
    val idConfig     = "none-sp5"
    val myNet        = Network(
      name = "my-net",
      external = true,
      driver = Network.BRIDGE,
      config = None
    )
    val logVolume    = Volume(hostPath = s"/test/logs/$idConfig", dockerPath = "/app/logs", mode ="rw")
    val workloadVol  = Volume(hostPath = "/test/workloads", dockerPath = "/app/workloads", mode ="rw")
    val sourceVolume = Volume(hostPath = s"/test/source", dockerPath = "/app/source", mode ="rw")
    val sinkVolume   = Volume(hostPath = s"/test/source", dockerPath = "/app/sink", mode ="rw")
    val N            = 3
    val basePort     = 9000
    val cs           = (0 until N).toList.map{ index =>
      Consumer(
        nodeId = NodeId(s"consumer-$index"),
        ports = Port.single(hostPort = basePort+index,dockerPort = basePort),
        networks = Networks.single(myNet),
        volumes = Volumes(
          logVolume,
          workloadVol,
          sourceVolume,
          sinkVolume
        ),
        environments = ConsumerEnvs(
          poolUrl = "http://pool-0:3000/api/v2",
          workloadFolder = "/app/workloads/trace-0",
          consumerIndex = index,
          consumerRate = 100
        ),
      )
    }
    val cf           = ComposeFile(
      version = "3",
      services = Services(cs.map(_.asInstanceOf[Service]):_* ),
      volumes = Volumes.empty,
      networks = Networks(myNet)
    )
    utils.toSave(s"./target/output/$idConfig.yml",cf.asJson(encoderComposeFile).asYaml.spaces4.getBytes)
  }

}
