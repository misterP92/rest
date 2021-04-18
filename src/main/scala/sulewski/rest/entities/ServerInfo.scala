package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ServerInfo(name: String, uptime: String, timesCalled: Int)

object ServerInfo {
  implicit val subscriptionEq: Eq[ServerInfo]           = Eq.fromUniversalEquals[ServerInfo]
  implicit val subscriptionDecoder: Decoder[ServerInfo] = deriveDecoder[ServerInfo]
  implicit val subscriptionEncoder: Encoder[ServerInfo] = deriveEncoder[ServerInfo]
}
