package sulewski.rest.domain

import java.lang.management.ManagementFactory

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import sulewski.rest.entities.ServerInfo
import sulewski.rest.exceptions.{InternalServerException, RestEndpointException}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object ServerInfoApi {
  private val ServerName: String = "Pattes Server ©"
  private val StartAccessedTimes: Int = 0
  sealed trait ServerInfoReplay

  object ServerInfoReplay {
    final case class ReplayObject(info: ServerInfo) extends ServerInfoReplay
    final case class ReplayError(error: RestEndpointException) extends ServerInfoReplay
  }

  sealed trait ServerInfoBaseCommand

  object ServerInfoBaseCommand {
    final case class FetchInfo(replyTo: ActorRef[ServerInfoReplay]) extends ServerInfoBaseCommand

    private[domain] final case class ReceivedInfo(info: ServerInfo, replyTo: ActorRef[ServerInfoReplay]) extends ServerInfoBaseCommand
    private[domain] final case class ReceivedError(error: RestEndpointException, replyTo: ActorRef[ServerInfoReplay]) extends ServerInfoBaseCommand
  }

  def apply()(implicit ec: ExecutionContext): Behavior[ServerInfoBaseCommand] = serverInformationRunner(new ServerInfoApi(), StartAccessedTimes)

  import ServerInfoBaseCommand._
  import ServerInfoReplay._
  private def serverInformationRunner(logic: ServerInfoApi, timesAccessed: Int): Behavior[ServerInfoBaseCommand] = Behaviors.receive { (context, message) =>
    message match {
      case FetchInfo(replyTo) =>
        val timesCalled = timesAccessed + 1
        context.pipeToSelf(logic.fetchInfo(replyTo, timesCalled)) {
          case Success(value) => value
          case Failure(exception) =>
            context.log.info(s"Rceived exception in [GetAll] with message: ${exception.getMessage}", exception)
            ReceivedError(InternalServerException(exception.getMessage), replyTo)
        }
        serverInformationRunner(logic, timesCalled)
      case ReceivedInfo(info, replyTo) =>
        replyTo ! ReplayObject(info)
        Behaviors.same
      case ReceivedError(error, replyTo) =>
        replyTo ! ReplayError(error)
        Behaviors.same
    }
  }
}

class ServerInfoApi()(implicit ec: ExecutionContext) {
  import ServerInfoApi._
  import ServerInfoApi.ServerInfoBaseCommand._

  def fetchInfo(replyTo: ActorRef[ServerInfoReplay], timesAccessed: Int): Future[ServerInfoBaseCommand] = {
    Future {
      val uptime = ManagementFactory.getRuntimeMXBean.getUptime / 1000
      ReceivedInfo(ServerInfo(ServerName, s"$uptime seconds", timesAccessed), replyTo)
    }
  }

}
