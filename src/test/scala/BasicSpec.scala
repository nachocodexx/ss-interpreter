import io.circe.{Encoder, Json}
import mx.cinvestav.{Interpreter, domain}
import mx.cinvestav.domain.{ComposeFile, Deploy, Image, Network, Networks, NodeId, Port, Ports, Services, Volume, Volumes, encoderComposeFile}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.yaml.syntax.AsYaml

class BasicSpec extends munit .CatsEffectSuite {

//  test("Basic"){
//    import mx.cinvestav.domain.Node
//    val logVolume            = Volume(s"/test/logs","/app/logs")
//    val mynet  = Network("mynet",Network.BRIDGE,external = true)
//
//    case class DataContainer(
//                              nodeId: NodeId,
//                              networks:Networks,
//                              image:Image,
//                              ports:Ports,
//                              profiles:List[String]=Nil,
//                              depends_on:List[String]=Nil,
//                              volumes:Volumes=Volumes.empty,
//                              secrets:List[String]=Nil,
//                              configs:List[String]=Nil,
//                              environments:Enviroments = Environmen
////                              Map[String,String]= Map.empty[String,String],
//                              metadata: domain.Metadata = domain.Metadata(data = Map.empty[String,String])
//      ) extends Node {
//
//      override def toJSON[A <: domain.Service](implicit encoder: Encoder[A]): Json = this.asInstanceOf[A].asJson(encoder = encoder)
//      override def name: String = nodeId.value
//      override def hostname: String = name
//    }
//
//    val dc0 = DataContainer(
//      nodeId = NodeId.auto("dc-"),
//      networks = Networks.empty,
//      image = Image("alfredohermoso/datacontainer","latest"),
//      ports = Port.singlePort(6969,6969),
//      volumes = Volumes(logVolume),
//      environment = Map("CACHE_REPLACEMENT_POLICY"-> "LRU")
//    )
//
//    val composeFile = ComposeFile(
//      version = "3",
//      services = Services(services = dc0),
//      volumes =Volumes(logVolume),
//      networks = Networks(mynet)
//    )
//
//    Interpreter.toSave("./target/output/datacontainer.yml",composeFile.asJson(encoderComposeFile).asYaml.spaces4.getBytes)
//
//  }

}
