package sulewski.rest.entities

import java.util.UUID

import akka.http.scaladsl.model.DateTime
import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class UserLogCreation(name: String,
                           log: Option[String]) {

  def toUser: UserLog =  {
    UserLog(
      id = UUID.randomUUID().toString,
      userName = name,
      creationDate = Some(DateTime.now.toString()),
      log = log
    )
  }
}

object UserLogCreation {
  implicit val subscriptionEq: Eq[UserLogCreation]           = Eq.fromUniversalEquals[UserLogCreation]
  implicit val subscriptionDecoder: Decoder[UserLogCreation] = deriveDecoder[UserLogCreation]
  implicit val subscriptionEncoder: Encoder[UserLogCreation] = deriveEncoder[UserLogCreation]
}