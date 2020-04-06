package sulewski.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import sulewski.rest.Main.logger
import sulewski.rest.Server.ServerConfig
import sulewski.rest.exceptions.InternalServerException
import sulewski.rest.routes.{Handlers, Router}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object Server {
  private val StartingPoint: Int = 0
  private val MaxAmountOfRetry: Int = 3
  private val HostName: String = "host"
  private val PortName: String = "port"

  def apply(router: Router, config: ServerConfig)(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer): Server = {
    new Server(router, config)(system, ec, mat)
  }

  final case class ServerConfig(host: String, port: Int)

  object ServerConfig {
    def apply(config: Config): ServerConfig = new ServerConfig(config.getString(HostName), config.getInt(PortName))
  }
}

class Server(router: Router, config: Server.ServerConfig)(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer) extends LazyLogging {
  import Server._

  def bind: Future[ServerBinding] = Http().bindAndHandle(router.route, config.host, config.port)

  def bindWithRetry: Future[ServerBinding] = {

    def innerBindWithRetry(currentRetry: Int): Future[ServerBinding] = {
      val promise: Promise[Http.ServerBinding] = Promise()
      if (currentRetry < MaxAmountOfRetry) this.bind.onComplete {
        case Success(value) =>
          logger.info(s"Everything works as expected: ${value.localAddress}")
          promise.complete(Success(value))
        case Failure(exception) =>
          logger.info(s"Something did not work: ${exception.getMessage}", exception)
          innerBindWithRetry(currentRetry + 1)
      } else promise.failure(InternalServerException("Could not start the server"))

      promise.future
    }

    innerBindWithRetry(StartingPoint)
  }
}
