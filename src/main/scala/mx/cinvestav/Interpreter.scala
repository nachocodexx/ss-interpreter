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
      val storageNodeImage   = Image(name="nachocode/storagenode",tag="v4")
      val sn0                = StorageNode(nodeId = NodeId("sn0"),image =storageNodeImage,ports=Ports(Port(4000,80)),networks = Networks.empty)
      val services           = Services()
        val servicesJson     = services.asJson(encoder = encoderServicesV2)
      val composeFile        = ComposeFile(version = "3",services =services ,volumes = Volumes.empty,networks = Networks.empty)
      val composeFileJson    = composeFile.asJson
      val composeAndServices = servicesJson.deepMerge(composeFileJson)
      val yaml               = composeAndServices.asYaml.spaces2
      println(yaml)
      toSave("./target/output/docker-compose.yml",yaml.getBytes).as(ExitCode.Success)
    }
  }
