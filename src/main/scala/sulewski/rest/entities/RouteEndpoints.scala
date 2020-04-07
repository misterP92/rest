package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class RouteEndpoints(pathBinding: String,
                          ids: Iterable[String],
                          supportedMethods: Iterable[String]) {
  def pathBindings: List[String] = List.empty
}

object RouteEndpoints {
  implicit val subscriptionEq: Eq[RouteEndpoints]           = Eq.fromUniversalEquals[RouteEndpoints]
  implicit val subscriptionDecoder: Decoder[RouteEndpoints] = deriveDecoder[RouteEndpoints]
  implicit val subscriptionEncoder: Encoder[RouteEndpoints] = deriveEncoder[RouteEndpoints]
}