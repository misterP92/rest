package sulewski.rest.entities

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object RoutedMockResponse {
  implicit val emailDecoder: Decoder[RoutedMockResponse] = deriveDecoder[RoutedMockResponse]
  implicit val emailEncoder: Encoder[RoutedMockResponse] = deriveEncoder[RoutedMockResponse]
  implicit val emailEq: Eq[RoutedMockResponse]           = Eq.fromUniversalEquals[RoutedMockResponse]

  sealed trait ResponseType { val underlying: StatusCode }

  object ResponseType {
    final case object Ok extends ResponseType {
      override val underlying: StatusCode = StatusCodes.OK
    }
    final case object NotFound extends ResponseType {
      override val underlying: StatusCode = StatusCodes.NotFound
    }
    final case object BadRequest extends ResponseType {
      override val underlying: StatusCode = StatusCodes.BadRequest
    }
    final case object Unauthorized extends ResponseType {
      override val underlying: StatusCode = StatusCodes.Unauthorized
    }
    final case object InternalServerError extends ResponseType {
      override val underlying: StatusCode = StatusCodes.InternalServerError
    }
    final case object BadGateway extends ResponseType {
      override val underlying: StatusCode = StatusCodes.BadGateway
    }
  }
}

final case class RoutedMockResponse(respType: RoutedMockResponse.ResponseType, body: io.circe.Json) {
  def toHttpResponse: HttpResponse = HttpResponse.apply(respType.underlying, entity = body.asString.get)
}
