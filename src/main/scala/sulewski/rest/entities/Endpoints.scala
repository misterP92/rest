package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Endpoints(pathBinding: String,
                     ids: Iterable[String],
                     supportedMethods: Iterable[String]) {
  def pathBindings: List[String] = List.empty
}

object Endpoints {
  implicit val subscriptionEq: Eq[Endpoints]           = Eq.fromUniversalEquals[Endpoints]
  implicit val subscriptionDecoder: Decoder[Endpoints] = deriveDecoder[Endpoints]
  implicit val subscriptionEncoder: Encoder[Endpoints] = deriveEncoder[Endpoints]
}