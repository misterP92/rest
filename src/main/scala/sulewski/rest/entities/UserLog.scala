package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class UserLog(id: String,
                         userName: String,
                         creationDate: Option[String],
                         log: Option[String])

object UserLog {
  implicit val subscriptionEq: Eq[UserLog]           = Eq.fromUniversalEquals[UserLog]
  implicit val subscriptionDecoder: Decoder[UserLog] = deriveDecoder[UserLog]
  implicit val subscriptionEncoder: Encoder[UserLog] = deriveEncoder[UserLog]
}