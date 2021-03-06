name := "sdf-play"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "javax.mail" % "mail" % "1.4.2",
  "commons-codec" % "commons-codec" % "1.8"
)

play.Project.playJavaSettings
