package sulewski.rest.domain

import akka.actor.typed.scaladsl.ActorContext

import scala.util.{Failure, Success, Try}

trait MessageHandler[T] {

  protected def handlePipeToSelf(msgName: String, context: ActorContext[T], fail: () => T): Try[T] => T = {
    case Success(value) => value
    case Failure(exception) =>
      context.log.info(s"Rceived exception in [$msgName] with message: ${exception.getMessage}", exception)
      fail()
  }

}
