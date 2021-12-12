package sulewski.rest.domain

import scala.language.{higherKinds, implicitConversions}

object Abstracted {
  trait Mappable[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  implicit class Constructed[X](underlying: X) {
    implicit def andThen[Y](next: X => Y): Y = next(underlying)
    implicit def andThenMap[F[_]: Mappable](next: X => F): F = next(underlying)
  }

  implicit class ConstructedMappable[XX, F[XX] : Mappable](underlying: F[XX]) {
    implicit def andThenMap[YY](next: XX => F[YY]): F[YY] = underlying.map()
  }
}

abstract class Abstracted[F[_] : Abstracted.Mappable, Response, Body, Result] {
  import Abstracted._
  def deSerializeRequest: HpRequest => Body
  def executeCommand: Body => F[Result]
  def serializeResponse: Result => F[Response]

  def handleRequest: HpRequest => F[Response] = { request =>
    val x = request andThen deSerializeRequest andThen executeCommand andThenMap serializeResponse
    x
  }
}
