package sulewski.rest.domain

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.typesafe.scalalogging.LazyLogging
import sulewski.rest.connectors.FileOperator
import sulewski.rest.entities.RouteEndpoints

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object EndpointApi extends LazyLogging {
  sealed trait BaseCommand

  sealed trait Replay
  final case class ReplaySeq(routingPoints: Seq[RouteEndpoints]) extends Replay
  final case class ReplayOption(routingPoint: Option[RouteEndpoints]) extends Replay

  object BaseCommand {
    sealed trait CommandWithReply
    final case class GetAll(replyTo: ActorRef[Replay]) extends BaseCommand
    final case class GetOne(id: String, replyTo: ActorRef[Replay]) extends BaseCommand

    private[domain] final case class ResultRouting(user: RouteEndpoints, replyTo: ActorRef[Replay]) extends BaseCommand
    private[domain] final case class MultiResultRouting(user: Seq[RouteEndpoints], replyTo: ActorRef[Replay]) extends BaseCommand
    private[domain] final case class NoRoutingPoints(replyTo: ActorRef[Replay]) extends BaseCommand
  }

  def apply(fileName: String)(implicit ec: ExecutionContext): Behavior[BaseCommand] = distribute(new EndpointApi(fileName))

  import BaseCommand._

  def distribute(api: EndpointApi)(implicit ec: ExecutionContext): Behavior[BaseCommand] = Behaviors.receive { (context, message) =>
    message match {
      case GetAll(replyTo) =>
        context.pipeToSelf(api.getAll(replyTo)) {
          case Success(value) => value
          case Failure(exception) =>
            context.log.info(s"Rceived exception in [GetAll] with message: ${exception.getMessage}", exception)
            NoRoutingPoints(replyTo)
        }
        Behaviors.same
      case GetOne(id, replyTo) =>
        context.pipeToSelf(api.get(id, replyTo)) {
          case Success(value) => value
          case Failure(exception) =>
            context.log.info(s"Rceived exception in [GetOne] with message: ${exception.getMessage}", exception)
            NoRoutingPoints(replyTo)
        }
        Behaviors.same
      case ResultRouting(routingEndpoint, replyTo) =>
        replyTo ! ReplayOption(Option(routingEndpoint))

        Behaviors.same
      case MultiResultRouting(routingEndPoints, replyTo) =>
        replyTo ! ReplaySeq(routingEndPoints)
        Behaviors.same
      case NoRoutingPoints(replyTo) =>
        replyTo ! ReplayOption(None)
        Behaviors.same
    }
  }
}

class EndpointApi(fileName: String)(implicit ec: ExecutionContext) extends FileOperator[RouteEndpoints] with HttpApiAkka[EndpointApi.BaseCommand, String, EndpointApi.Replay] with LazyLogging {
  import EndpointApi.BaseCommand._
  import EndpointApi._

  override def get(id: String, replyTo: ActorRef[Replay]): Future[BaseCommand] = {
    logger.info("Inside get by id")
    getAllInternal.map(_.find(_.ids.exists(_.equals(id))).map(ResultRouting(_, replyTo)).getOrElse(NoRoutingPoints(replyTo)))
  }

  override def getAll(replyTo: ActorRef[Replay]): Future[BaseCommand] = {
    logger.info("Inside get all")
    getAllInternal.map(MultiResultRouting(_, replyTo))
  }

  private def getAllInternal: Future[Seq[RouteEndpoints]] = readFileAsIterableClass(fileName).map { routingEndpoints =>
    logger.debug(s"${routingEndpoints.map(_.pathBindings)}")
    routingEndpoints
  }

  override def post(toCreate: String, replyTo: ActorRef[Replay]): Future[BaseCommand] = Future.successful(NoRoutingPoints(replyTo))
}
