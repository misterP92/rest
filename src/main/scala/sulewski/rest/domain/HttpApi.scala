package sulewski.rest.domain

import scala.concurrent.Future

trait HttpApi[T] {

  def get(id: String): Future[Option[T]]

  def getAll: Future[Seq[T]]

}
