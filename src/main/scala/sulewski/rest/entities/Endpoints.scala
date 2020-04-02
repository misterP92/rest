package sulewski.rest.entities

import java.time.LocalDateTime

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedObjectEncoder

case class Endpoints(id: String,
                     `web-space`: Option[String] = None,
                     `subscription-type`: Option[String] = None,
                     `register-date`: Option[LocalDateTime] = None,
                     characteristics: Map[String, String] = Map.empty)

object Endpoints {
  implicit val subscriptionEq: Eq[Endpoints]           = Eq.fromUniversalEquals[Endpoints]
  implicit val subscriptionDecoder: Decoder[Endpoints] = deriveDecoder[Endpoints]
  implicit val subscriptionEncoder: Encoder[Endpoints] = deriveEncoder[Endpoints]
}