package sulewski.rest.entities

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RoutedMockResponse(respType: RoutedMockResponse.ResponseType, body: io.circe.Json) {
  def toHttpResponse: HttpResponse = HttpResponse.apply(respType.underlying, entity = body.asString.get)
}

object RoutedMockResponse {
  sealed trait ResponseType { val underlying: StatusCode }

  object ResponseType {
    final case object Ok extends ResponseType { override val underlying: StatusCode = StatusCodes.OK }
    final case object NotFound extends ResponseType { override val underlying: StatusCode = StatusCodes.NotFound }
    final case object BadRequest extends ResponseType { override val underlying: StatusCode = StatusCodes.BadRequest }
    final case object Unauthorized extends ResponseType { override val underlying: StatusCode = StatusCodes.Unauthorized }
    final case object InternalServerError extends ResponseType { override val underlying: StatusCode = StatusCodes.InternalServerError }
    final case object BadGateway extends ResponseType { override val underlying: StatusCode = StatusCodes.BadGateway }

    implicit val ResponseTypeDecoder: Decoder[ResponseType] = deriveDecoder[ResponseType]
    implicit val ResponseTypeEncoder: Encoder[ResponseType] = deriveEncoder[ResponseType]
    implicit val ResponseTypeEq: Eq[ResponseType]           = Eq.fromUniversalEquals[ResponseType]
  }

  implicit val routedMockResponseDecoder: Decoder[RoutedMockResponse] = deriveDecoder[RoutedMockResponse]
  implicit val routedMockResponseEncoder: Encoder[RoutedMockResponse] = deriveEncoder[RoutedMockResponse]
  implicit val routedMockResponseEq: Eq[RoutedMockResponse]           = Eq.fromUniversalEquals[RoutedMockResponse]
}
