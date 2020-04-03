package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Endpoints(pathBindings: List[String],
                     ids: Seq[String] = Seq.empty,
                     methods: Seq[String] = Seq.empty,
                     body: Option[io.circe.Json] = None)

object Endpoints {
  implicit val subscriptionEq: Eq[Endpoints]           = Eq.fromUniversalEquals[Endpoints]
  implicit val subscriptionDecoder: Decoder[Endpoints] = deriveDecoder[Endpoints]
  implicit val subscriptionEncoder: Encoder[Endpoints] = deriveEncoder[Endpoints]
}