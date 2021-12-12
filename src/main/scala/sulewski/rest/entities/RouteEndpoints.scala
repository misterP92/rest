package sulewski.rest.entities

import cats.Eq
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

final case class RouteEndpoints(pathBinding: String,
                                ids: Iterable[String],
                                supportedMethods: Map[RouteEndpoints.HttpMethod, Json]) {
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

  sealed trait HttpMethod { val value: String }

  object HttpMethod {
    final case object GetMethod extends HttpMethod { override val value: String = "Get" }
    final case object PostMethod extends HttpMethod { override val value: String = "Post" }
    final case object PutMethod extends HttpMethod { override val value: String = "Put" }
    final case object PatchMethod extends HttpMethod { override val value: String = "Patch" }

    implicit val decodeHttpMethod: Decoder[HttpMethod] = Decoder[String].emap {
      case "Get" => Right(GetMethod)
      case "Post" => Right(PostMethod)
      case "Put" => Right(PutMethod)
      case "Patch" => Right(PatchMethod)
      case x => Left(s"Failed with decoding the HttpMethod: $x")
    }
    implicit val encodeHttpMethod: Encoder[HttpMethod] = Encoder.instance {
      case GetMethod => GetMethod.value.asJson
      case PostMethod => PostMethod.value.asJson
      case PutMethod => PutMethod.value.asJson
      case PatchMethod => PatchMethod.value.asJson
    }
  }

  implicit val subscriptionEq: Eq[RouteEndpoints]           = Eq.fromUniversalEquals[RouteEndpoints]
  implicit val subscriptionDecoder: Decoder[RouteEndpoints] = deriveDecoder[RouteEndpoints]
  implicit val subscriptionEncoder: Encoder[RouteEndpoints] = deriveEncoder[RouteEndpoints]
}