package sulewski.rest.routes

import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.StrictLogging
import sulewski.rest.domain.ApiOnDemand
import sulewski.rest.entities.RouteEndpoints
import sulewski.rest.entities.RouteEndpoints.PathBindings
import sulewski.rest.exceptions.InternalServerException

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

object OnDemandRouter {
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

  private def bindPaths(binding: PathBindings, pathEnd: Route, alreadyDefinedRoute: Option[Route]): Option[Route] = {
    val newBinding = pathPrefix(binding.api) {
      logger.info(s"Biding api [${binding.api}] ")
      binding.pathToResource.foldRight(pathEnd){ (one, two) =>
        logger.info(s"Biding $one with [$two] ")
        pathPrefix(one) { two } }
    }
    if(alreadyDefinedRoute.nonEmpty) alreadyDefinedRoute.map(_ ~ newBinding) else Some(newBinding)
  }

  @tailrec
  private def routeMethods(supportedMethods: List[String], binding: PathBindings, maybeRoute: Option[Route]): Route =
    supportedMethods match {
      case Nil => maybeRoute.getOrElse(throw CannotCreateDynamicRoutesError)
      case "Get" :: tail =>
        val pathEndGet = get {
          logger.info(s"Biding get request")
          complete(brains.getAll)
        }
        routeMethods(tail, binding, bindPaths(binding, pathEndGet, maybeRoute))
      case "Post" :: tail =>
        val pathEndPost = post {
          logger.info(s"Biding post request")
          complete(brains.getAll)
        }
        routeMethods(tail, binding, bindPaths(binding, pathEndPost, maybeRoute))
      case "Put" :: tail =>
        val pathEndPut = put {
          logger.info(s"Biding put request")
          complete(brains.getAll)
        }
        routeMethods(tail, binding, bindPaths(binding, pathEndPut, maybeRoute))
      case "Patch" :: tail =>
        val pathEndPatch = patch {
          logger.info(s"Biding patch request")
          complete(brains.getAll)
        }
        routeMethods(tail, binding, bindPaths(binding, pathEndPatch, maybeRoute))
      case _ :: tail => routeMethods(tail, binding, maybeRoute)
    }

  private def oneRoute(oneEndpoint: RouteEndpoints): Route = oneEndpoint.pathBindings match {
    case Some(bindings) => routeMethods(oneEndpoint.supportedMethods.toList, bindings, None)
    case None => throw InternalServerException("Failed during routing")
  }

  override def route: Route = recRoute(routes.toList, None)
}
