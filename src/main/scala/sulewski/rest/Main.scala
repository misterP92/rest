package sulewski.rest


import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, DispatcherSelector}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import sulewski.rest.connectors.FileOperator
import sulewski.rest.domain.{EndpointApi, UserManagementApi}
import sulewski.rest.entities.RouteEndpoints
import sulewski.rest.routes.{ActorReferences, RestRoute}
import sulewski.rest.routes.RestRoute.RouteConfig

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}

object Main extends StrictLogging {
  val AtMost: Duration = 2.seconds
  val Host: String = "0.0.0.0"
  val Port: Int = 9000

  private def dispatcherFromContext[T](context: ActorContext[T]): ExecutionContextExecutor = context.system.dispatchers.lookup(DispatcherSelector.default())

  def main(args: Array[String]): Unit = {
    logger.info(s"Starting running the server")
    val config = ConfigFactory.parseResources("Inmem-rest-server.conf").resolve().getConfig("sulewski.rest")

    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val apis = getApiEndpoints(config.getConfig("routes"))(dispatcherFromContext(context))
      val routeConfig = RouteConfig(config)
      val endpointRegistryActor = context.spawn(EndpointApi(routeConfig.fileName)(dispatcherFromContext(context)), "EndpointRegistryActor")
      context.watch(endpointRegistryActor)
      val userRegistryActor = context.spawn(UserManagementApi()(dispatcherFromContext(context)), "UserRegistryActor")
      context.watch(userRegistryActor)

      val references = ActorReferences(endpointRegistryActor, userRegistryActor)

      logger.info(s"Will be creating endpoint for paths: ${apis.map(_.pathBindings)}")
      val routes = RestRoute.apply(routeConfig, apis, references)(dispatcherFromContext(context), context.system)
      val server = Server(routes, Server.ServerConfig(config))(context.system, dispatcherFromContext(context))
      server.bindWithRetry

      logger.info(s"Will be creating endpoint for paths: ${apis.map(_.pathBindings)}")

      Behaviors.empty
    }

    val system: ActorSystem[Nothing] = ActorSystem[Nothing](rootBehavior, "pattes-system")
    system.logConfiguration()
  }

  def getApiEndpoints(config: Config)(implicit ec: ExecutionContext):Seq[RouteEndpoints] = {
    val routeConf = RouteConfig(config)
    val parser = new FileOperator[RouteEndpoints] {}
    Await.result(parser.readFileAsClass(routeConf.fileName), AtMost)
  }
}
