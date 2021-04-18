package sulewski.rest.connectors

import io.circe.Decoder

import scala.concurrent.{ExecutionContext, Future}

object FileConnector {
  type Path = String
}

trait FileConnector[T] {
  import FileConnector._

  def readFileAsClass(path: Path)(implicit ec: ExecutionContext, dec: Decoder[T]): Future[Option[T]]

  def readFileAsIterableClass(path: Path)(implicit ec: ExecutionContext, dec: Decoder[T]): Future[Seq[T]]

  def readFileAsJson(path: Path)(implicit ec: ExecutionContext): Future[io.circe.Json]

  def writeToFile(fileName: String, body: String)(implicit ec: ExecutionContext): Future[String]
}
