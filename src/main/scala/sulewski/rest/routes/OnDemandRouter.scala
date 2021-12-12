package sulewski.rest.routes

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import com.typesafe.scalalogging.StrictLogging
import io.circe.Json
import sulewski.rest.domain.{ApiOnDemand, HpRequest}
import sulewski.rest.entities.RouteEndpoints
import sulewski.rest.entities.RouteEndpoints.PathBindings
import sulewski.rest.exceptions.InternalServerException

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

object OnDemandRouter {
  private val EmptyString: String = ""
  private val CannotCreateDynamicRoutes: String = "Could not create dynamic routes"
  private val CannotCreateDynamicRoutesError = InternalServerException(CannotCreateDynamicRoutes)
}

class OnDemandRouter(routes: Seq[RouteEndpoints])(implicit ec: ExecutionContext) extends Router with Directives with StrictLogging {
  import OnDemandRouter._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  lazy val brains: ApiOnDemand = new ApiOnDemand

  @tailrec
  private def recRoute(routes: List[RouteEndpoints], route: Option[Route]): Route =
    routes match {
      case Nil => route.getOrElse(throw CannotCreateDynamicRoutesError)
      case rHead :: rTail =>
        val pathBindIngRoute = oneRoute(rHead)
        recRoute(rTail, route.map(_ ~ pathBindIngRoute).orElse(Some(pathBindIngRoute)))
    }

  private def bindPaths(binding: PathBindings, idInPath: Option[String], pathEnd: Route, alreadyDefinedRoute: Option[Route]): Option[Route] = {
    val logAndBindPath: (String, Route) => Route = (pathPart, theRoute) => {
      logger.info(s"Biding $pathPart with [$theRoute] ")
      pathPrefix(pathPart) { theRoute }
    }

    val newBinding = pathPrefix(binding.api) {
      logger.info(s"Biding api [${binding.api}] ")
      binding.pathToResource.foldRight(pathEnd) {
        case ("#id#", two) => logAndBindPath(idInPath.getOrElse(EmptyString), two)
        case (one, two) => logAndBindPath(one, two)
      }
    }
    if(alreadyDefinedRoute.nonEmpty) alreadyDefinedRoute.map(_ ~ newBinding) else Some(newBinding)
  }

  private def routeMethods(binding: PathBindings, pathIds: Iterable[String], maybeRouteToUse: Option[Route], toRoute: Map[RouteEndpoints.HttpMethod, Json]): Route = {
    def logAndHandleMessage: (String, Json) => StandardRoute = (loggerMsg, jsonValue) => {
      logger.info(loggerMsg)
      val hpResponse = brains.handle(HpRequest(jsonValue))
      complete(hpResponse.map(_.asJsString)) // complete(StatusCode.int2StatusCode(200), brains.handle(jsonValue))
    }

    def mountRoutes: (Option[Route], Route) => Option[Route] = (mountedRoutes, endRoute) => {
      if(pathIds.nonEmpty) {
        pathIds.foldLeft(mountedRoutes) { (restOfRoutes, idInPath) => bindPaths(binding, Some(idInPath), endRoute, restOfRoutes) }
      } else bindPaths(binding, None, endRoute, mountedRoutes)
    }

    @tailrec
    def inner(maybeRoute: Option[Route], listToRoute: List[(RouteEndpoints.HttpMethod, Json)]): Route = {
      listToRoute match {
        case Nil => maybeRoute.getOrElse(throw CannotCreateDynamicRoutesError)
        case (RouteEndpoints.HttpMethod.GetMethod, jsonValue) :: tail =>
          val pathEndGet = get { logAndHandleMessage("Biding get request", jsonValue) }
          inner(mountRoutes(maybeRoute, pathEndGet), tail)
        case (RouteEndpoints.HttpMethod.PostMethod, jsonValue) :: tail =>
          val pathEndPost = post { logAndHandleMessage("Biding post request", jsonValue) }
          inner(mountRoutes(maybeRoute, pathEndPost), tail)
        case (RouteEndpoints.HttpMethod.PutMethod, jsonValue) :: tail =>
          val pathEndPut = put { logAndHandleMessage("Biding put request", jsonValue) }
          inner(mountRoutes(maybeRoute, pathEndPut), tail)
        case (RouteEndpoints.HttpMethod.PatchMethod, jsonValue) :: tail =>
          val pathEndPatch = patch { logAndHandleMessage("Biding patch request", jsonValue) }
          inner(mountRoutes(maybeRoute, pathEndPatch), tail)
        case _ :: tail => inner(maybeRoute, tail)
      }
    }

    inner(maybeRouteToUse, toRoute.toList)
  }

  private def oneRoute(oneEndpoint: RouteEndpoints): Route = oneEndpoint.pathBindings match {
    case Some(bindings) => routeMethods(bindings, oneEndpoint.ids, None, oneEndpoint.supportedMethods)
    case None => throw InternalServerException("Failed during routing")
  }

  override def route: Route = recRoute(routes.toList, None)
}
