package sulewski.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import sulewski.rest.Main.logger
import sulewski.rest.exceptions.InternalServerException
import sulewski.rest.routes.{Handlers, Router}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object Server {
  private val StartingPoint: Int = 0
  private val MaxAmountOfRetry: Int = 3
  def apply(router: Router, host: String, port: Int)(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer): Server = {
    new Server(router, host, port)(system, ec, mat)
  }
}

class Server(router: Router, host: String, port: Int)(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer) extends LazyLogging {
  import Server._

  def bind: Future[ServerBinding] = Http().bindAndHandle(router.route, host, port)

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
