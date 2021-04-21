package sulewski.rest


import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior, DispatcherSelector}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import sulewski.rest.connectors.FileOperator
import sulewski.rest.domain.{EndpointApi, ServerInfoApi, UserManagementApi}
import sulewski.rest.entities.RouteEndpoints
import sulewski.rest.routes.{ActorReferences, RestRoute}
import sulewski.rest.routes.RestRoute.RouteConfig

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}

object Main extends StrictLogging {
  private val TickStart: Int = 0
  val AtMost: Duration = 2.seconds
  val Host: String = "0.0.0.0"
  val Port: Int = 9000

  private def dispatcherFromContext[T](context: ActorContext[T]): ExecutionContextExecutor = context.system.dispatchers.lookup(DispatcherSelector.default())

  sealed trait RootCommands
  object RootCommands {
    final case class Start(system: ActorSystem[RootCommands]) extends RootCommands
    final case class ReloadApp(routeEndPoints: Seq[RouteEndpoints]) extends RootCommands
    final case object Tick extends RootCommands
  }

  def rootBehaviourTick(references: ActorReferences, config: Config, apis: Seq[RouteEndpoints], tweak: Int): Behavior[RootCommands] = Behaviors.receiveMessage {
    case RootCommands.Start(system) =>
      logger.info(s"Will be creating endpoint for paths: ${apis.map(_.pathBindings)}")
      val routes = RestRoute.apply(apis, references)(system.dispatchers.lookup(DispatcherSelector.default()), system)
      val server = Server(routes, Server.ServerConfig(config))(system, system.dispatchers.lookup(DispatcherSelector.default()))
      server.bindWithRetry
      logger.info(s"Will be creating endpoint for paths: ${apis.map(_.pathBindings)}")
      Behaviors.same
    case RootCommands.ReloadApp(routeEndPoints) =>
      logger.info(s"Reloading the application with new routes: $routeEndPoints")
      systemStart(config, Some(routeEndPoints))
      Behaviors.stopped(() => logger.info("I am dying!"))
    case RootCommands.Tick =>
      logger.info("Received a tick to the main frame")
      rootBehaviourTick(references, config, apis, tweak + 1)
  }

  private def mainBehaviour(config: Config, reloadedRoutes: Option[Seq[RouteEndpoints]]): Behavior[RootCommands] = Behaviors.setup[RootCommands] { context =>
    val apis = reloadedRoutes.getOrElse(getApiEndpoints(config.getConfig("routes"))(dispatcherFromContext(context)))
    val routeConfig = RouteConfig(config)
    val endpointRegistryActor = context.spawn(EndpointApi(routeConfig.fileName)(dispatcherFromContext(context)), "EndpointRegistryActor")
    context.watch(endpointRegistryActor)
    val userRegistryActor = context.spawn(UserManagementApi()(dispatcherFromContext(context)), "UserRegistryActor")
    context.watch(userRegistryActor)
    val serverInfoRegistryActor = context.spawn(ServerInfoApi()(dispatcherFromContext(context)), "ServerInfoRegistryActor")
    context.watch(serverInfoRegistryActor)

    val references = ActorReferences(endpointRegistryActor, userRegistryActor, serverInfoRegistryActor)

    rootBehaviourTick(references, config, apis, TickStart)
  }

  private def systemStart(config: Config, apis: Option[Seq[RouteEndpoints]]): Unit = {
    val rootBehavior = mainBehaviour(config, apis)
    val system: ActorSystem[RootCommands] = ActorSystem[RootCommands](rootBehavior, "pattes-system")
    system.logConfiguration()
    system.tell(RootCommands.Start(system))
  }

  def main(args: Array[String]): Unit = {
    logger.info(s"Starting running the server")
    val config = ConfigFactory.parseResources("Inmem-rest-server.conf").resolve().getConfig("sulewski.rest")
    systemStart(config, None)
  }

  def getApiEndpoints(config: Config)(implicit ec: ExecutionContext):Seq[RouteEndpoints] = {
    val routeConf = RouteConfig(config)
    val parser = new FileOperator[RouteEndpoints] {}
    Await.result(parser.readFileAsIterableClass(routeConf.fileName), AtMost)
  }
}
