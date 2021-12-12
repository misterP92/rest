name := "sulewski.rest"

version := "0.1.0"

scalaVersion := "2.12.8"

resolvers += Resolver.typesafeIvyRepo("releases")

resolvers += "Iota" at "http://clojars.org/repo/"

val catsRevision = "1.1.0"
val akkaHttpRevision = "10.2.4"
val circeRevision = "0.9.3"
val akkaRevision = "2.6.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka"          %% "akka-http"            % akkaHttpRevision,
  "com.typesafe.akka"          %% "akka-http-spray-json" % akkaHttpRevision,
  "com.typesafe.akka"          %% "akka-stream"          % akkaRevision, 
  "com.typesafe.akka"          %% "akka-actor-typed"     % akkaRevision,
  "org.typelevel"              %% "cats-core"            % catsRevision,
  "org.typelevel"              %% "cats-effect"          % catsRevision,
  "iota"                       %  "iota"                 % "1.1.3",
  "io.circe"                   %% "circe-core"           % circeRevision,
  "io.circe"                   %% "circe-parser"         % circeRevision,
  "io.circe"                   %% "circe-generic"        % circeRevision,
  "io.circe"                   %% "circe-generic-extras" % circeRevision,
  "com.typesafe.play"          %% "play-json"            % "2.9.2",
  "ch.qos.logback"             % "logback-classic"       % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.0",
  "de.heikoseeberger"          %% "akka-http-circe"      % "1.21.0",

  "org.specs2"        %% "specs2-core"       % "4.2.0"      % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpRevision % Test
)


dockerBaseImage := "armhf/openjdk:8-jre-alpine"
dockerRepository := Some("patte")

packageName in Docker := "sulewski-akka-http-rest"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)

mainClass in Compile := Some("sulewski.rest.Main")