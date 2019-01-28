name := "sulewski.rest"

version := "0.1"

scalaVersion := "2.12.8"

resolvers += Resolver.typesafeIvyRepo("releases")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.specs2" %% "specs2-core" % "4.2.0" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.3" % Test
)


dockerBaseImage := "armhf/openjdk:8-jre-alpine"
dockerRepository := Some("patte")

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)

mainClass in Compile := Some("sulewski.rest.Main")