package sulewski.rest.domain
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import io.circe.{Json, Printer}

import scala.concurrent.{ExecutionContext, Future}

class ApiOnDemand(implicit ec: ExecutionContext) extends HttpApi[Json] {
  override def get(id: String): Future[Option[Json]] = Future.successful(io.circe.parser.parse("""{}""").toOption)

  override def handle(responseBody: Json): Future[Json] = {
    Future(responseBody)//.map{x => println(x); x}
  }
}
