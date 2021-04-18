package sulewski.rest.connectors

import java.io.{File, PrintWriter}

import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import sulewski.rest.connectors.FileConnector.Path
import io.circe._
import io.circe.parser._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.{Codec, Source}

object FileOperator {
  val Appender: String = ""

  class RichFile( file: File) {

    def text: String = Source.fromFile(file)(Codec.UTF8).mkString

    def text_=( s: String ) {
      val out = new PrintWriter( file , "UTF-8")
      try{ out.print( s ) }
      finally{ out.close() }
    }
  }

  object RichFile {
    import scala.language.implicitConversions
    implicit def enrichFile( file: File ): RichFile = new RichFile( file )

  }
}

trait FileOperator[T] extends FileConnector[T] with LazyLogging {
  import FileOperator._
  import FileOperator.RichFile.enrichFile

  override def readFileAsClass(path: Path)(implicit ec: ExecutionContext, dec: Decoder[T]): Future[Seq[T]] = {
    readFileAsJson(path).map { jsonValue =>

      jsonValue.as[Seq[T]].fold( failure => {
        logger.info(s"Received failure when parsing result ${failure.getMessage}", failure)
        Seq.empty
      }, goodResult => goodResult)
    }
  }



  override def readFileAsJson(path: Path)(implicit ec: ExecutionContext): Future[Json] = readAsString(path).map(x => parse(x).getOrElse(Json.Null))

  private def readAsString(path: Path)(implicit ec: ExecutionContext): Future[String] =
    Future(scala.io.Source.fromInputStream(getClass.getResourceAsStream(path)).getLines().mkString(Appender))

  override def writeToFile(fileName: String, body: String)(implicit ec: ExecutionContext): Future[String] = {
    Future {
      val file = new File(s"/$fileName.json")
      file.text = body
      file.text
    }
  }
}
