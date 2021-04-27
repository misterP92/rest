package sulewski.rest.entities

import cats.Eq
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RouteEndpoints(pathBinding: String,
                                ids: Iterable[String],
                                supportedMethods: Map[String, Json]) {
  import RouteEndpoints._
  def pathBindings: Option[RouteEndpoints.PathBindings] = pathBinding.split(PathSeparator).tail.toList match {
    case Nil => None
    case api :: pathRest => Some(RouteEndpoints.PathBindings(api, pathRest))
    case _ => None
  }
}

object RouteEndpoints {
  private val PathSeparator: String = "/"

  final case class PathBindings(api: String, pathToResource: List[String])

  object PathBindings {
    implicit val PathBindingsEq: Eq[PathBindings]           = Eq.fromUniversalEquals[PathBindings]
    implicit val PathBindingsDecoder: Decoder[PathBindings] = deriveDecoder[PathBindings]
    implicit val PathBindingsEncoder: Encoder[PathBindings] = deriveEncoder[PathBindings]
  }

  implicit val subscriptionEq: Eq[RouteEndpoints]           = Eq.fromUniversalEquals[RouteEndpoints]
  implicit val subscriptionDecoder: Decoder[RouteEndpoints] = deriveDecoder[RouteEndpoints]
  implicit val subscriptionEncoder: Encoder[RouteEndpoints] = deriveEncoder[RouteEndpoints]
}