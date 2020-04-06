package sulewski.rest

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import sulewski.rest.routes.RestRoute

import scala.concurrent.ExecutionContextExecutor

object Main extends StrictLogging {
  val Host: String = "0.0.0.0"
  val Port: Int = 9000

  def main(args: Array[String]): Unit = {
    logger.info(s"Starting running the server")
    val config = ConfigFactory.parseResources("Inmem-rest-server.conf").resolve().getConfig("sulewski.rest")

    implicit val system: ActorSystem = ActorSystem("pattes-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val server = Server(RestRoute.apply(config.getConfig("routes")), Server.ServerConfig(config))(system, system.dispatcher, materializer)

    server.bindWithRetry
    logger.info(s"Server running on $Host:$Port")
  }
}
