package sulewski.rest.domain

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.typesafe.scalalogging.LazyLogging
import sulewski.rest.connectors.FileOperator
import sulewski.rest.entities.RouteEndpoints

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object EndpointApi extends LazyLogging with MessageHandler[EndpointApi.BaseCommand] {
  sealed trait BaseCommand

  sealed trait Replay
  final case class ReplaySeq(routingPoints: Seq[RouteEndpoints]) extends Replay
  final case class ReplayOption(routingPoint: Option[RouteEndpoints]) extends Replay

  object BaseCommand {
    sealed trait CommandWithReply
    final case class GetAll(replyTo: ActorRef[Replay]) extends BaseCommand
    final case class GetOne(id: String, replyTo: ActorRef[Replay]) extends BaseCommand
    final case class ReplaceCurrent(newEndpoints: RouteEndpoints) extends BaseCommand

    private[domain] final case class ResultRouting(user: RouteEndpoints, replyTo: ActorRef[Replay]) extends BaseCommand
    private[domain] final case class MultiResultRouting(user: Seq[RouteEndpoints], replyTo: ActorRef[Replay]) extends BaseCommand
    private[domain] final case class NoRoutingPoints(replyTo: ActorRef[Replay]) extends BaseCommand
    private[domain] final case class AsyncAnswer(wasSuccessful: Boolean) extends BaseCommand
  }

  def apply(fileName: String)(implicit ec: ExecutionContext): Behavior[BaseCommand] = distribute(new EndpointApi(fileName), handleAsync)

  import BaseCommand._

  private def distribute(api: EndpointApi, handleAsync: (ActorContext[BaseCommand], AsyncAnswer) => Behavior[BaseCommand])
                        (implicit ec: ExecutionContext): Behavior[BaseCommand] = Behaviors.receive { (context, message) =>
    message match {
      case GetAll(replyTo) =>
        context.pipeToSelf(api.getAll(replyTo))(handlePipeToSelf("GetAll", context, () => NoRoutingPoints(replyTo)))
        Behaviors.same
      case GetOne(id, replyTo) =>
        context.pipeToSelf(api.get(id, replyTo))(handlePipeToSelf("GetOne", context, () => NoRoutingPoints(replyTo)))
        Behaviors.same
      case ReplaceCurrent(endPoints) =>
        context.pipeToSelf(api.postAsync(endPoints))(handlePipeToSelf("ReplaceCurrent", context, () => AsyncAnswer(false)))
        Behaviors.same
      case msg: AsyncAnswer => handleAsync(context, msg)
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

  private def handleAsync: (ActorContext[BaseCommand], AsyncAnswer) => Behavior[BaseCommand] = {
    case (context, AsyncAnswer(true)) =>
      context.log.debug("Successfully changed endpoint configuration in the database")
      Behaviors.same
    case (context, AsyncAnswer(false)) =>
      context.log.error("Was not able to change the endpoint configuration in the database, restart will change endpoint to default")
      Behaviors.same
  }
}

class EndpointApi(fileName: String)(implicit ec: ExecutionContext) extends FileOperator[RouteEndpoints] with HttpAkkaApi[EndpointApi.BaseCommand, RouteEndpoints, EndpointApi.Replay] with LazyLogging {
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

  override def post(toCreate: RouteEndpoints, replyTo: ActorRef[Replay]): Future[BaseCommand] = Future.successful(NoRoutingPoints(replyTo))

  def postAsync(toCreate: RouteEndpoints): Future[BaseCommand] = {
    println(toCreate)
    println("Will change the file name: ")
    println(fileName)
    Future.successful(AsyncAnswer(true))
  }
}
