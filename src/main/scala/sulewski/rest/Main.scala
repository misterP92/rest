package sulewski.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import sulewski.rest.routes.{Handlers, RestRoute}

import scala.concurrent.ExecutionContextExecutor

object Main extends StrictLogging {
  val Host: String = "0.0.0.0"
  val Port: Int = 9000

  def main(args: Array[String]): Unit = {
    logger.info(s"Starting running the server")

    implicit val system: ActorSystem = ActorSystem("pattes-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher


    val server = Server(RestRoute.apply, Host, Port)(system, system.dispatcher, materializer)

    server.bindWithRetry //Http().bindAndHandle(route, "0.0.0.0", 8080)
    logger.info(s"Server running on $Host:$Port")
  }
}
