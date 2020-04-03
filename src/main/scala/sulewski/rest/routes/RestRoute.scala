package sulewski.rest.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

object RestRoute {
  def apply: RestRoute = new RestRoute()
}

class RestRoute extends Router {

  implicit def rejectionHandle: RejectionHandler = Handlers.rejectionHandle
  implicit def exceptionHandle: ExceptionHandler = Handlers.exceptionHandler

  private val endpointRoutes: EndpointRoutes = new EndpointRoutes
  val route: Route = {
    pathPrefix("api") {
      endpointRoutes.route
    }
  }
}
