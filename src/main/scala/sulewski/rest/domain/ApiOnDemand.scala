package sulewski.rest.domain
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import io.circe.{Json, Printer}
import sulewski.rest.domain.HpResponse.{HpCode, SuccessHpResponse, FailedHpResponse, circeJsonToPlayJson}

import scala.concurrent.{ExecutionContext, Future}

class ApiOnDemand(implicit ec: ExecutionContext) extends HttpApi[HpRequest, HpResponse] { //io.circe.parser.parse("""{}""").toOption
  override def get(id: String): Future[Option[HpResponse]] = Future.successful(Option(SuccessHpResponse(HpCode.Ok, play.api.libs.json.Json.parse("{}"))))

  override def handle(responseBody: HpRequest): Future[HpResponse] = {
    val requestBody = circeJsonToPlayJson(responseBody.body)

    Future(requestBody match {
      case Some(body) => SuccessHpResponse(HpCode.Ok, body)
      case None => FailedHpResponse(HpCode.BadRequest, play.api.libs.json.Json.parse("""{"status": "Worngly formatted body"}"""))
    })
  }
}
