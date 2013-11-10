name := "sdf-play"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "postgresql" % "postgresql" % "9.1-901.jdbc4"
)     

play.Project.playJavaSettings
