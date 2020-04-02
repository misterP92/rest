package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


case class Identification(id: String, randomAvatar: String = Identification.rand)


object Identification {
  implicit val identificationDecoder: Decoder[Identification] = deriveDecoder[Identification]
  implicit val identificationEncoder: Encoder[Identification] = deriveEncoder[Identification]
  implicit val identificationEq: Eq[Identification]           = Eq.fromUniversalEquals[Identification]

  def rand: String = {
    "............."
  }

}
