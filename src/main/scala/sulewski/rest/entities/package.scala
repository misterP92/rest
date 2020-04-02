package sulewski.rest

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._

package object entities {

  implicit val converter = io.circe.Decoder[T]


  implicit class EmailExt(e: Email) {
    override def toString: String = s"${e.email}@${e.domain}"

    def fromString(emailAsString: Option[String]): Option[Email] =
      emailAsString match {
        case Some(emailString) =>
          val parts = emailString.split("@")

          parts.lastOption match {
            case Some(someDomain) =>
              parts.headOption.map(
                Email(_, someDomain)
              )
            case None => None
          }
        case None => None
      }
  }
}
