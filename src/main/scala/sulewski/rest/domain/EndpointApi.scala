package sulewski.rest.domain

import com.typesafe.scalalogging.LazyLogging
import sulewski.rest.connectors.FileOperator
import sulewski.rest.entities.RouteEndpoints

import scala.concurrent.{ExecutionContext, Future}

class EndpointApi(fileName: String)(implicit ec: ExecutionContext) extends FileOperator[RouteEndpoints] with LazyLogging {

  def get(id: String): Future[Option[RouteEndpoints]] = {
    logger.info("Inside get by id")
    getAll.map(_.find(_.ids.exists(_.equals(id))))
  }

  def getAll: Future[Seq[RouteEndpoints]] = {
    logger.info("Inside get all")
    //Future.successful(Seq(Endpoints("path/to/stuff", Iterable("my", "stuff"), Iterable("Get", "Post")), Endpoints("path/to/givers", Iterable("1234", "6543"), Iterable("Get", "Put")) ))
    readFileAsClass(fileName)
  }
}
