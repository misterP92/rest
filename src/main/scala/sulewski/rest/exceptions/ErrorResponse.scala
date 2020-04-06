package sulewski.rest.exceptions

import cats.Eq
import io.circe.{Decoder, Encoder, Json}
import io.circe.parser._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ErrorResponse(status: String, code: String, detail: String) extends Exception(detail) {
  def render: String =
    s"""
      |{
      |  "statusCode": "$status",
      |  "message": "$code",
      |  "details": "$detail"
      |}
    """.stripMargin

  def toJson: io.circe.Json = parse(this.render).getOrElse(Json.Null)
}

object ErrorResponse {
  implicit val ErrorResponseEq: Eq[ErrorResponse]           = Eq.fromUniversalEquals[ErrorResponse]
  implicit val ErrorResponseDecoder: Decoder[ErrorResponse] = deriveDecoder[ErrorResponse]
  implicit val ErrorResponseEncoder: Encoder[ErrorResponse] = deriveEncoder[ErrorResponse]

}