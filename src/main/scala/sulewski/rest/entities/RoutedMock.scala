package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object RoutedMock {
  implicit val emailDecoder: Decoder[RoutedMock] = deriveDecoder[RoutedMock]
  implicit val emailEncoder: Encoder[RoutedMock] = deriveEncoder[RoutedMock]
  implicit val emailEq: Eq[RoutedMock]           = Eq.fromUniversalEquals[RoutedMock]

  sealed trait Operation
  object Operation {
    final case object Get extends Operation
    final case object Post extends Operation
    final case object Put extends Operation
    final case object Patch extends Operation
  }
}

final case class RoutedMock(id: Option[String],
                            operation: Option[RoutedMock.Operation],
                            paths: List[String], body: Option[io.circe.Json],
                            response: Option[RoutedMockResponse])
