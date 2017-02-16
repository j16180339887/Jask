
name := """jask"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "com.fasterxml.jackson.core" % "jackson-databind" % "latest.milestone"
)

// I got a lot of bugs when I using elasticsearch, so I decide to use http post instead
// libraryDependencies += "org.elasticsearch" % "elasticsearch" % "latest.milestone"
// libraryDependencies += ("org.elasticsearch.client" % "transport" % "latest.milestone").excludeAll(ExclusionRule(organization = "io.netty"))

//javaOptions in run += "-Dhttp.port=80"
