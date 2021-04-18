package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RoutedMock(id: Option[String],
                            operation: Option[Operation],
                            paths: List[String], body: Option[io.circe.Json],
                            response: Option[RoutedMockResponse])

object RoutedMock {

  implicit val routedMockDecoder: Decoder[RoutedMock] = deriveDecoder[RoutedMock]
  implicit val routedMockEncoder: Encoder[RoutedMock] = deriveEncoder[RoutedMock]
  implicit val routedMockEq: Eq[RoutedMock]           = Eq.fromUniversalEquals[RoutedMock]
}
