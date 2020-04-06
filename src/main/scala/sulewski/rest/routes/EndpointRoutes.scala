package sulewski.rest.routes

import akka.http.scaladsl.server.{Directives, Route}
import sulewski.rest.domain.EndpointApi
import sulewski.rest.entities.Endpoints._
import sulewski.rest.exceptions.NotFoundException

import scala.concurrent.ExecutionContext

object EndpointRoutes {
  private val EndpointName: String = "endpoint"
  private val EndPointNotAvailable: String = "Endpoint was not available for provided id"
}

class EndpointRoutes(fileName: String)(implicit ec: ExecutionContext) extends Router with Directives {
  import EndpointRoutes._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  //import io.circe.generic.auto._

  private lazy val endpointLogic: EndpointApi = new EndpointApi(fileName)
  val route: Route = get {
    path(EndpointName / RemainingPath) { date =>
      val result = endpointLogic.get(date.toString()).map(_.getOrElse(throw NotFoundException(EndPointNotAvailable)))

      complete(result)
    } ~ path(EndpointName) {
      complete(endpointLogic.getAll)
    }
  }
}
