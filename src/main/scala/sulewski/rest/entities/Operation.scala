package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

sealed trait Operation

object Operation {
  final case object Get extends Operation
  final case object Post extends Operation
  final case object Put extends Operation
  final case object Patch extends Operation

  implicit val OperationDecoder: Decoder[Operation] = deriveDecoder[Operation]
  implicit val OperationEncoder: Encoder[Operation] = deriveEncoder[Operation]
  implicit val OperationEq: Eq[Operation]           = Eq.fromUniversalEquals[Operation]
}