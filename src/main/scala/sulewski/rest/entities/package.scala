package sulewski.rest

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

package object entities {
  //implicit val converter: Decoder[Seq[Endpoints]] = deriveDecoder[Seq[Endpoints]]

}
