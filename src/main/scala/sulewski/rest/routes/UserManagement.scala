package sulewski.rest.routes

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import sulewski.rest.Main.RootCommands
import sulewski.rest.domain.UserManagementApi
import sulewski.rest.domain.UserManagementApi.BaseLogCommand.{CreateUserLog, GetAllUserLogs, GetOneUserLog}
import sulewski.rest.domain.UserManagementApi.{UserLogReplay, UserLogReplayOption, UserLogReplaySeq}
import sulewski.rest.entities.UserLogCreation
import sulewski.rest.entities.UserLogCreation._
import sulewski.rest.exceptions.NotFoundException

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object UserManagement {
  private val UsersName: String = "users"
  private val OneUser: String = "fetchOne"
  private val UserNotAvailable: String = "User was not available for provided id"
  def apply(underlyingLogic: ActorRef[UserManagementApi.BaseLogCommand])(implicit ec: ExecutionContext, system: ActorSystem[RootCommands]): UserManagement = new UserManagement(underlyingLogic)
}

class UserManagement(underlyingLogic: ActorRef[UserManagementApi.BaseLogCommand])(implicit ec: ExecutionContext, system: ActorSystem[RootCommands]) extends Router with Directives {
  import UserManagement._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  implicit val timeout: Timeout = 3.seconds

  override val route: Route = pathPrefix(UsersName)(users)

  def users: Route = concat(
    path(OneUser / RemainingPath) { uri =>
      val result = underlyingLogic.ask[UserLogReplay](GetOneUserLog(uri.toString(), _)).map {
        case UserLogReplaySeq(users) => users.headOption
        case UserLogReplayOption(maybeUser) => maybeUser
      }.map(_.getOrElse(throw NotFoundException(UserNotAvailable)))

      complete(StatusCode.int2StatusCode(200), result)
    },
    post {
      decodeRequest {
        entity(as[UserLogCreation]) { user =>
          val result = underlyingLogic.ask[UserLogReplay](CreateUserLog(user, _)).map {
            case UserLogReplaySeq(users) => users.headOption
            case UserLogReplayOption(maybeUser) => maybeUser
          }.map(_.getOrElse(throw NotFoundException(UserNotAvailable)))

          complete(StatusCode.int2StatusCode(201), result)
        }
      }
    },
    get {
      val result = underlyingLogic.ask[UserLogReplay](GetAllUserLogs).map {
        case UserLogReplaySeq(users) => users
        case UserLogReplayOption(maybeUser) => maybeUser.toSeq
      }

      complete(result)
    }
  )
}
