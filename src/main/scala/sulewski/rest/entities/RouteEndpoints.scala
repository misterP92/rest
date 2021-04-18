package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RouteEndpoints(pathBinding: String,
                          ids: Iterable[String],
                          supportedMethods: Iterable[String]) {
  import RouteEndpoints._
  def pathBindings: Option[RouteEndpoints.PathBindings] = pathBinding.split(PathSeparator).tail.toList match {
    case Nil => None
    case api :: pathRest => Some(RouteEndpoints.PathBindings(api, pathRest.mkString(PathSeparator)))
    case _ => None
  }
}

object RouteEndpoints {
  //private val ApiName: String = "api"
  private val PathSeparator: String = "/"

  final case class PathBindings(api: String, pathToResource: String)

  object PathBindings {
    implicit val PathBindingsEq: Eq[PathBindings]           = Eq.fromUniversalEquals[PathBindings]
    implicit val PathBindingsDecoder: Decoder[PathBindings] = deriveDecoder[PathBindings]
    implicit val PathBindingsEncoder: Encoder[PathBindings] = deriveEncoder[PathBindings]
  }

  implicit val subscriptionEq: Eq[RouteEndpoints]           = Eq.fromUniversalEquals[RouteEndpoints]
  implicit val subscriptionDecoder: Decoder[RouteEndpoints] = deriveDecoder[RouteEndpoints]
  implicit val subscriptionEncoder: Encoder[RouteEndpoints] = deriveEncoder[RouteEndpoints]
}