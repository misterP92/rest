package sulewski.rest.connectors

import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import sulewski.rest.connectors.FileConnector.Path
import io.circe._
import io.circe.parser._

import scala.concurrent.{ExecutionContext, Future}

object FileOperator {
  val Appender: String = ""
}

class FileOperator[T] extends FileConnector[T] with LazyLogging {
  import FileOperator._

  override def readFileAsClass(path: Path)(implicit ec: ExecutionContext, dec: Decoder[T]): Future[Seq[T]] = {
    println(path)
    readFileAsJson(path).map { jsonValue =>

      jsonValue.as[Seq[T]].fold( failure => {
        logger.info(s"Received failure when parsing result ${failure.getMessage}", failure)
        Seq.empty
      }, goodResult => goodResult)
    }
  }

  override def readFileAsJson(path: Path)(implicit ec: ExecutionContext): Future[Json] = readAsString(path).map(x => parse(x).getOrElse(Json.Null))

  private def readAsString(path: Path)(implicit ec: ExecutionContext): Future[String] = Future(scala.io.Source.fromInputStream( getClass.getResourceAsStream(path)).getLines().mkString(Appender))
}
