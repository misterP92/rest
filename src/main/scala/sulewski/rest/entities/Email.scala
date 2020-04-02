package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Email(email: String, domain: String = "gmail.com")


object Email {
  implicit val emailDecoder: Decoder[Email] = deriveDecoder[Email]
  implicit val emailEncoder: Encoder[Email] = deriveEncoder[Email]
  implicit val emailEq: Eq[Email]           = Eq.fromUniversalEquals[Email]
}
