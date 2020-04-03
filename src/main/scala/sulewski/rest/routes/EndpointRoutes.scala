package sulewski.rest.routes

import akka.http.scaladsl.server.{Directives, Route}
import sulewski.rest.domain.EndpointApi
import sulewski.rest.entities.Endpoints._

class EndpointRoutes extends Router with Directives {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  private val endpointLogic: EndpointApi = new EndpointApi
  val route: Route = get {
    path("endpoint" / RemainingPath) { date =>
      complete(endpointLogic.get(date.toString()))
    } ~ path("endpoint") {
      complete(endpointLogic.getAll)
    } //~ path("health") {
    //  complete(Unicorn.render)
    //}
  }
}
