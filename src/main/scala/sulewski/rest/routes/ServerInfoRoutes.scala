package sulewski.rest.routes

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import sulewski.rest.domain.ServerInfoApi
import sulewski.rest.domain.ServerInfoApi.ServerInfoBaseCommand.FetchInfo
import sulewski.rest.domain.ServerInfoApi.ServerInfoReplay.{ReplayError, ReplayObject}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object ServerInfoRoutes {
  private val ServerInfoName: String = "serverInfo"
  //private val ServerInfoNotAvailable: String = "Server Info was not available for provided id"

  def apply(underlyingLogic: ActorRef[ServerInfoApi.ServerInfoBaseCommand])(implicit ec: ExecutionContext, system: ActorSystem[_]): ServerInfoRoutes = new ServerInfoRoutes(underlyingLogic)
}

class ServerInfoRoutes(underlyingLogic: ActorRef[ServerInfoApi.ServerInfoBaseCommand])
                      (implicit ec: ExecutionContext, system: ActorSystem[_]) extends Router with Directives {
  import ServerInfoRoutes._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  implicit val timeout: Timeout = 3.seconds

  override val route: Route = get {
    path(ServerInfoName) {
      val result = underlyingLogic.ask[ServerInfoApi.ServerInfoReplay](FetchInfo).map {
        case ReplayObject(info) => info
        case ReplayError(error) => throw error
      }

      complete(result)
    }
  }
}
