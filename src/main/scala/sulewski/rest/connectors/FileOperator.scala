package sulewski.rest.connectors

import java.io.{File, PrintWriter}

import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import sulewski.rest.connectors.FileConnector.Path
import io.circe._
import io.circe.parser._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.{Codec, Source}
import scala.util.Try

object FileOperator {
  private val BasePath: String = "target/scala-2.12/classes/"
  private val JsonExt: String = ".json"
  val Appender: String = ""

  implicit class RichFile(file: File) {
    def text: String = Source.fromFile(file)(Codec.UTF8).mkString

    def appendText(s: String) {
      val out = new PrintWriter( file , "UTF-8")
      try{ out.print( s ) }
      finally{ out.close() }
    }
  }
}

trait FileOperator[T] extends FileConnector[T] with LazyLogging {
  import FileOperator._

  override def readFileAsClass(path: Path)(implicit ec: ExecutionContext, dec: Decoder[T]): Future[Option[T]] = {
    readFileAsJson(path).map { jsonValue =>

      jsonValue.as[T].fold( failure => {
        logger.error(s"Received failure when parsing result ${failure.getMessage}", failure)
        None
      }, goodResult => Some(goodResult))
    }
  }

  override def readFileAsIterableClass(path: Path)(implicit ec: ExecutionContext, dec: Decoder[T]): Future[Seq[T]] = {
    readFileAsJson(path).map { jsonValue =>

      jsonValue.as[Seq[T]].fold( failure => {
        logger.error(s"Received failure when parsing result ${failure.getMessage}", failure)
        Seq.empty
      }, goodResult => goodResult)
    }
  }

  override def readFileAsJson(path: Path)(implicit ec: ExecutionContext): Future[Json] = readAsString(path).map(x => parse(x).getOrElse(Json.Null))

  private def readAsString(path: Path)(implicit ec: ExecutionContext): Future[String] =
    Future(scala.io.Source.fromInputStream(getClass.getResourceAsStream(path)).getLines().mkString(Appender))

  def readJsonFileNames()(implicit ec: ExecutionContext): Future[Seq[String]] = Future {
    Try(new File(BasePath).listFiles.filter(_.isFile).filter(_.getName.endsWith(JsonExt)).map(_.getName).toSeq).toOption.toSeq.flatten
  }

  override def writeToFile(fileName: String, body: String)(implicit ec: ExecutionContext): Future[String] = {
    Future {
      val file = new File(s"$BasePath$fileName.json")
      file.appendText(body)
      file.text
    }
  }
}
