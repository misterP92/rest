package sulewski.rest.exceptions
import akka.http.javadsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.BadRequest

final case class BadRequestException(override val msg: String) extends RestEndpointException {
  override val statusCode: StatusCode = BadRequest
}
