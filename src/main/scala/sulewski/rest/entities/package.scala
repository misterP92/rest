package sulewski.rest

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._

package object entities {

  implicit val converter: Decoder[Endpoints] = io.circe.Decoder[Endpoints]

}
