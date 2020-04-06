package sulewski.rest.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object RestRoute {
  private val Api: String = "api"

  final case class MyConfig(fileName: String)
  private object MyConfig {
    val FileNameConfigName: String = "endpointPath"
  }

  def apply(config: Config)(implicit ec: ExecutionContext): RestRoute = new RestRoute(config)
}

class RestRoute(config: Config)(implicit ec: ExecutionContext) extends Router with Handlers {
  import RestRoute._
  private val currentConfig = extractConfig

  private lazy val endpointRoutes: EndpointRoutes = new EndpointRoutes(currentConfig.fileName)

  val route: Route = handleExceptions(exceptionHandle) {
    pathPrefix(Api) {
      endpointRoutes.route
    }
  }

  def extractConfig: MyConfig = {
    MyConfig(fileName = config.getString(MyConfig.FileNameConfigName))
  }
}
