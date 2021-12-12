package sulewski.rest.domain

import akka.actor.typed.ActorRef

import scala.concurrent.Future

trait HttpAkkaApi[T, U, A] {

  def get(id: String, replyTo: ActorRef[A]): Future[T]

  def getAll(replyTo: ActorRef[A]): Future[T]

  def post(toCreate: U, replyTo: ActorRef[A]): Future[T]

}
