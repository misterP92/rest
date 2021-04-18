package sulewski.rest.domain

import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors
import sulewski.rest.connectors.FileOperator
import sulewski.rest.entities.{UserLog, UserLogCreation}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object UserManagementApi {
  sealed trait UserLogReplay
  final case class UserLogReplaySeq(users: Seq[UserLog]) extends UserLogReplay
  final case class UserLogReplayOption(maybeUser: Option[UserLog]) extends UserLogReplay

  sealed trait BaseLogCommand

  object BaseLogCommand {
    final case class GetAllUserLogs(replyTo: ActorRef[UserLogReplay]) extends BaseLogCommand
    final case class GetOneUserLog(id: String, replyTo: ActorRef[UserLogReplay]) extends BaseLogCommand
    final case class CreateUserLog(user: UserLogCreation, replyTo: ActorRef[UserLogReplay]) extends BaseLogCommand
    final case class CreateUserLogAsync(user: UserLogCreation, replyTo: ActorRef[UserLogReplay]) extends BaseLogCommand
    private[domain] final case class UserLogResult(user: UserLog, replyTo: ActorRef[UserLogReplay]) extends BaseLogCommand
    private[domain] final case class UserLogMultiResult(user: Seq[UserLog], replyTo: ActorRef[UserLogReplay]) extends BaseLogCommand
    private[domain] final case class NoUserLog(replyTo: ActorRef[UserLogReplay]) extends BaseLogCommand
  }


  def apply()(implicit ec: ExecutionContext): Behavior[BaseLogCommand] = distribute(new UserManagementApi)

  import BaseLogCommand._

  def distribute(userMgnApi: UserManagementApi): Behavior[BaseLogCommand] = Behaviors.receive { (context, message) =>
    message match {
      case GetAllUserLogs(replyTo) =>
        context.pipeToSelf(userMgnApi.getAll(replyTo)){
          case Success(value) => value
          case Failure(exception) =>
            context.log.info(s"Rceived exception in [GetAllUsers] with message: ${exception.getMessage}", exception)
            NoUserLog(replyTo)
        }
        Behaviors.same
      case GetOneUserLog(id, replyTo) =>
        context.pipeToSelf(userMgnApi.get(id, replyTo)){
          case Success(value) => value
          case Failure(exception) =>
            context.log.info(s"Rceived exception in [GetOneUsers] with message: ${exception.getMessage}", exception)
            NoUserLog(replyTo)
        }
        Behaviors.same
      case CreateUserLog(user, replyTo) =>
        context.pipeToSelf(userMgnApi.post(user, replyTo)){
          case Success(value) => value
          case Failure(exception) =>
            context.log.info(s"Rceived exception in [CreateUser] with message: ${exception.getMessage}", exception)
            NoUserLog(replyTo)
        }
        Behaviors.same
      case CreateUserLogAsync(user, replyTo) =>
        context.pipeToSelf(userMgnApi.post(user, replyTo)){
          case Success(value) => value
          case Failure(exception) =>
            context.log.info(s"Rceived exception in [CreateUserAsync] with message: ${exception.getMessage}", exception)
            NoUserLog(replyTo)
        }
        Behaviors.same
      case UserLogResult(user, replyTo) =>
        replyTo ! UserLogReplayOption(Some(user))
        Behaviors.same

      case UserLogMultiResult(users, replyTo) =>
        replyTo ! UserLogReplaySeq(users)
        Behaviors.same
      case NoUserLog(replyTo) =>
        replyTo ! UserLogReplayOption(None)
        Behaviors.same
    }
  }

}

import io.circe.syntax._
class UserManagementApi()(implicit ec: ExecutionContext) extends HttpApiAkka[UserManagementApi.BaseLogCommand, UserLogCreation, UserManagementApi.UserLogReplay] with FileOperator[UserLog] {
  import UserManagementApi.BaseLogCommand._
  import UserManagementApi._

  override def get(id: String, replyTo: ActorRef[UserLogReplay]): Future[UserManagementApi.BaseLogCommand] = {
    logger.info("Inside get")
    readFileAsClass(s"/$id.json").map(_.map(UserLogResult(_, replyTo)).getOrElse(NoUserLog(replyTo)))
  }

  override def getAll(replyTo: ActorRef[UserLogReplay]): Future[UserManagementApi.BaseLogCommand] = {
    logger.info("Inside get all")

    for {
      listOfFiles <- readJsonFileNames
      appendedFiles = listOfFiles.map(file => s"/$file")
      _ = {
        logger.debug(s"List of json files found: $appendedFiles")
      }
      logs <- Future.sequence(appendedFiles.map(readFileAsClass(_))).map(_.flatten)
    } yield  if (logs.nonEmpty) UserLogMultiResult(logs, replyTo) else NoUserLog(replyTo)
  }

  override def post(user: UserLogCreation, replyTo: ActorRef[UserLogReplay]): Future[UserManagementApi.BaseLogCommand] = {
    logger.info("Inside post")
    val newUser = user.toUser
    writeToFile(newUser.id, newUser.asJson.noSpaces).map(_ => UserLogResult(newUser, replyTo))
  }
}
