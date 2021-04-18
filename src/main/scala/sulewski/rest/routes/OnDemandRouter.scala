package sulewski.rest.routes

import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.{Directives, Route}
import sulewski.rest.domain.ApiOnDemand
import sulewski.rest.entities.RouteEndpoints
import sulewski.rest.entities.RouteEndpoints.PathBindings
import sulewski.rest.exceptions.InternalServerException

import scala.concurrent.ExecutionContext

object OnDemandRouter {
  private val CannotCreateDynamicRoutes: String = "Could not create dynamic routes"
  private val CannotCreateDynamicRoutesError = InternalServerException(CannotCreateDynamicRoutes)
}

class OnDemandRouter(routes: Seq[RouteEndpoints])(implicit ec: ExecutionContext) extends Router with Directives {
  import OnDemandRouter._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  //import io.circe.generic.auto._

  lazy val brains: ApiOnDemand = new ApiOnDemand

  private def recRoute(routes: List[RouteEndpoints], route: Option[Route]): Route =
    routes match {
      case Nil => route.getOrElse(throw CannotCreateDynamicRoutesError)
      case rHead :: rTail =>
        val pathBindIngRoute = oneRoute(rHead)
        recRoute(rTail, route.map(_ ~ pathBindIngRoute).orElse(Some(pathBindIngRoute)))
    }

  private def routeMethods(supportedMethods: List[String], binding: PathBindings, maybeRoute: Option[Route]): Route =
    supportedMethods match {
      case Nil => maybeRoute.getOrElse(throw CannotCreateDynamicRoutesError)
      case "Get" :: tail =>
        println("HEJJJJJJJJJJJ")
        val newBinding = pathPrefix(binding.api) {
          get {
            println(s"TJAAaAAAAA:          ${binding.pathToResource}")
            path("/"+binding.pathToResource) {
              println("STRINGGGGGSSSSSS")
              complete(brains.getAll)
            }
          }
        }
        println(newBinding)
        routeMethods(tail, binding, maybeRoute.map(_ ~ newBinding).orElse(Some(newBinding)))
      case "Post" :: tail =>
        val newBinding = post { path(binding.pathToResource) { complete() }}
        routeMethods(tail, binding, maybeRoute.map(_ ~ newBinding).orElse(Some(newBinding)))
      case "Put" :: tail =>
        val newBinding = put { path(binding.pathToResource) { complete() }}
        routeMethods(tail, binding, maybeRoute.map(_ ~ newBinding).orElse(Some(newBinding)))
      case _ :: tail => routeMethods(tail, binding, maybeRoute)
    }

  private def oneRoute(oneEndpoint: RouteEndpoints): Route = oneEndpoint.pathBindings match {
    case Some(bindings) =>
      println(s"BINNNNND:    $bindings")
      routeMethods(oneEndpoint.supportedMethods.toList, bindings, None)
    case None => throw InternalServerException("Failed during routing")
  }

  override def route: Route = recRoute(routes.toList, None)
}
