package sulewski.rest.domain

import io.circe.Json

import scala.concurrent.Future

trait HttpApi[X, Y] {

  def get(id: String): Future[Option[Y]]

  def handle(responseBody: X): Future[Y]

}
