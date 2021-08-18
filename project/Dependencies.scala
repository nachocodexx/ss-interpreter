import sbt._


object Dependencies {
  def apply(): Seq[ModuleID] = {
    lazy val PureConfig = "com.github.pureconfig" %% "pureconfig" % "0.15.0"
    lazy val MUnitCats ="org.typelevel" %% "munit-cats-effect-3" % "1.0.3" % Test
    lazy val Log4Cats =   "org.typelevel" %% "log4cats-slf4j"   % "2.1.1"
    lazy val CirceYAML ="io.circe" %% "circe-yaml" % "0.12.0"
    lazy val Fs2 = "co.fs2" %% "fs2-core" % "3.0.6"
    lazy val ApacheCommons = "org.apache.commons" % "commons-lang3" % "3.12.0"

    val circeVersion = "0.14.1"
    lazy val Circe =Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
    Seq(PureConfig,MUnitCats,Log4Cats,CirceYAML,Fs2,ApacheCommons)++Circe
  }
}


