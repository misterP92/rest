package sulewski.rest.domain

import io.circe.Json

import scala.concurrent.Future

trait HttpApi[T] {

  def get(id: String): Future[Option[T]]

  def handle(responseBody: Json): Future[T]

}
