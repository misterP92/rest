package sulewski.rest.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.config.Config
import sulewski.rest.entities.RouteEndpoints

import scala.concurrent.ExecutionContext
import scala.util.Try

object RestRoute {
  private val Api: String = "api"

  final case class RouteConfig(fileName: String)
  object RouteConfig {
    private val FileNameConfigName: String = "endpointPath"

    def apply(config: Config): RouteConfig = new RouteConfig(Try(config.getString(FileNameConfigName)).toOption.getOrElse("/endpoints/Endpoints.json"))
  }

  def apply(config: RouteConfig, routes: Seq[RouteEndpoints], references: ActorReferences)(implicit ec: ExecutionContext, system: ActorSystem[_]): RestRoute = new RestRoute(config, routes, references)
}

class RestRoute(config: RestRoute.RouteConfig, routes: Seq[RouteEndpoints], references: ActorReferences)(implicit ec: ExecutionContext, system: ActorSystem[_]) extends Router with Handlers {
  import RestRoute._

  private lazy val endpointRoutes: EndpointRoutes = new EndpointRoutes(config.fileName, references.endpointRegistryActor)
  private lazy val userRoutes: UserManagement = UserManagement(references.userRegistryActor)
  private lazy val onDemandRoutes: OnDemandRouter = new OnDemandRouter(routes)

  val route: Route = handleExceptions(exceptionHandle) {
    pathPrefix(Api) {
      endpointRoutes.route ~ userRoutes.route
    } ~ onDemandRoutes.route
  }
}
