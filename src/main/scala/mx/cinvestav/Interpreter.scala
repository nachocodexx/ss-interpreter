package mx.cinvestav
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import io.circe.Json
import io.circe.yaml._
import io.circe.yaml.syntax._
import io.circe.generic.auto._
import io.circe.syntax._
import mx.cinvestav.domain.{encoderServicesV2, _}

import java.io.{BufferedOutputStream, FileOutputStream}

object Interpreter extends IOApp{
    def toSave(outputPath:String,data:Array[Byte]): IO[Unit] = {
      val buff = new BufferedOutputStream(new FileOutputStream(outputPath)).pure[IO]
      Resource.make[IO,BufferedOutputStream](buff)(x=>IO.delay(x.close())).use(x=>IO.delay(x.write(data)))
    }
    override def run(args: List[String]): IO[ExitCode] = {
//      case class SingleService(image:String,ports:List[String],networks:List[String])
//      case class Service(image:String,ports:List[String])
//      case class Services(services:Map[String,Service])
//      val sn0 = Service(image = "nachocode/storage-node",List("4000:80"))
//      val servicesData = Map("sn0"->sn0,"sn1"->sn0)

      val storageNodeImage  = Image(name="nachocode/storagenode",tag="v4")
      val sn0 = StorageNode(nodeId = NodeId("sn0"),image =storageNodeImage,ports=List(Port(4000,80)),networks = Networks.empty,storageVolume = ???)
      val services = Services(services = List())
        val servicesJson = services.asJson(encoder = encoderServicesV2)
      val composeFile = ComposeFile(version = "3",services =services ,volumes = Nil,networks = Networks.empty)
      val composeFileJson = composeFile.asJson
      val composeAndServices = servicesJson.deepMerge(composeFileJson)
      val yaml = composeAndServices.asYaml.spaces2
      println(yaml)
      toSave("./target/output/docker-compose.yml",yaml.getBytes).as(ExitCode.Success)
    }
  }
