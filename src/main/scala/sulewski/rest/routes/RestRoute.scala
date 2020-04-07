package sulewski.rest.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object RestRoute {
  private val Api: String = "api"

  final case class RouteConfig(fileName: String)
  private object RouteConfig {
    val FileNameConfigName: String = "endpointPath"

    def apply(config: Config): RouteConfig = new RouteConfig(config.getString(RouteConfig.FileNameConfigName))
  }

  def apply(config: Config)(implicit ec: ExecutionContext): RestRoute = new RestRoute(RouteConfig(config))
}

class RestRoute(config: RestRoute.RouteConfig)(implicit ec: ExecutionContext) extends Router with Handlers {
  import RestRoute._

  private lazy val endpointRoutes: EndpointRoutes = new EndpointRoutes(config.fileName)

  val route: Route = handleExceptions(exceptionHandle) {
    pathPrefix(Api) {
      endpointRoutes.route
    }
  }
}
