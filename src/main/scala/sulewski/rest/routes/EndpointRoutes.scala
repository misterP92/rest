package sulewski.rest.routes

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import sulewski.rest.Main.RootCommands
import sulewski.rest.domain.EndpointApi
import sulewski.rest.domain.EndpointApi.BaseCommand.{GetAll, GetOne}
import sulewski.rest.domain.EndpointApi.{Replay, ReplayOption, ReplaySeq}
import sulewski.rest.entities.RouteEndpoints
import sulewski.rest.entities.RouteEndpoints._
import sulewski.rest.exceptions.NotFoundException

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


object EndpointRoutes {
  private val EndpointName: String = "endpoint"
  private val EndPointNotAvailable: String = "Endpoint was not available for provided id"

  def apply(underlyingLogic: ActorRef[EndpointApi.BaseCommand])(implicit ec: ExecutionContext, system: ActorSystem[RootCommands]): EndpointRoutes =
    new EndpointRoutes(underlyingLogic)
}

class EndpointRoutes(underlyingLogic: ActorRef[EndpointApi.BaseCommand])(implicit ec: ExecutionContext, system: ActorSystem[RootCommands]) extends Router with Directives {
  import EndpointRoutes._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  //import io.circe.generic.auto._
  implicit val timeout: Timeout = 3.seconds

  val route: Route = get {
    path(EndpointName / RemainingPath) { date =>
      val result = underlyingLogic.ask[Replay](GetOne(date.toString(), _)).map {
        case ReplaySeq(endpoints) => endpoints.headOption
        case ReplayOption(maybeEndpoint) => maybeEndpoint
      }.map(_.getOrElse(throw NotFoundException(EndPointNotAvailable)))

      complete(result)
    } ~ path(EndpointName) {
      val result = underlyingLogic.ask[Replay](GetAll).map {
        case ReplaySeq(endpoints) => endpoints
        case ReplayOption(maybeEndpoint) => maybeEndpoint.toSeq
      }
      complete(result)
    }
  } ~ changeOnDemandRoutes


  private def changeOnDemandRoutes: Route = post {
    path("reloadRoutes") {
      decodeRequest {
        entity(as[RouteEndpoints]) { routeEndPoints =>
          system.tell(RootCommands.ReloadApp(Seq(routeEndPoints)))
          complete(StatusCode.int2StatusCode(201), "{}")
        }
      }
    }
  }
}
