lazy val app = (project in file("."))
                .settings(
                    name := "interpreter",
                    version := "0.1",
                    scalaVersion := "2.13.6",
                    libraryDependencies ++=Dependencies(),
                    assemblyJarName:= "interpreter"
                )
