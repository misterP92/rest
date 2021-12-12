package sulewski.rest

import play.api.libs.json.JsValue

import scala.util.Try

package object domain {

  final case class HpRequest(body: io.circe.Json, headers: Map[String, String] = Map.empty[String, String])

  object HpResponse {
    sealed trait HpCode {
      val code: Int
      val status: String
    }
    object HpCode {
      sealed trait SuccessfulCode extends HpCode
      sealed trait FailedCode extends HpCode

      /** 200 codes */
      final case object Ok extends SuccessfulCode {
        override val code: Int = 200
        override val status: String = "OK"
      }

      final case object Created extends SuccessfulCode {
        override val code: Int = 201
        override val status: String = "Created"
      }

      final case object NoContent extends SuccessfulCode {
        override val code: Int = 204
        override val status: String = "No Content"
      }

      /** 400 codes */
      final case object BadRequest extends FailedCode {
        override val code: Int = 400
        override val status: String = "Bad Request"
      }

      final case object Unauthorized extends FailedCode {
        override val code: Int = 401
        override val status: String = "Unauthorized"
      }

      final case object Forbidden extends FailedCode {
        override val code: Int = 403
        override val status: String = "Forbidden"
      }

      final case object NotFound extends FailedCode {
        override val code: Int = 404
        override val status: String = "Not Found"
      }

      final case object MethodNotAllowed extends FailedCode {
        override val code: Int = 405
        override val status: String = "Method Not Allowed"
      }

      final case object RequestTimeout extends FailedCode {
        override val code: Int = 408
        override val status: String = "Request Timeout"
      }

      final case object Conflict extends FailedCode {
        override val code: Int = 409
        override val status: String = "Conflict"
      }

      /** 500 codes */
      final case object InternalServerError extends SuccessfulCode {
        override val code: Int = 500
        override val status: String = "Internal Server Error"
      }

      final case object NotImplemented extends SuccessfulCode {
        override val code: Int = 501
        override val status: String = "Not Implemented"
      }

      final case object BadGateway extends SuccessfulCode {
        override val code: Int = 502
        override val status: String = "Bad Gateway"
      }

      final case object ServiceUnavailable extends SuccessfulCode {
        override val code: Int = 503
        override val status: String = "Service Unavailable"
      }
    }
    import play.api.libs.json.Json

    final case class SuccessHpResponse(override val code: HpCode, override val body: JsValue) extends HpResponse
    final case class FailedHpResponse(override val code: HpCode, override val body: JsValue) extends HpResponse

    def circeJsonToPlayJson(circeJson: io.circe.Json): Option[JsValue] = Try(Json.parse(circeJson.noSpaces)).toOption
  }

  import HpResponse._
  sealed trait HpResponse {
    val code: HpCode
    val body: JsValue
    def asJsString: String = body.toString()
  }
}
