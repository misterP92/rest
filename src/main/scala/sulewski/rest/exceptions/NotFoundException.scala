package sulewski.rest.exceptions
import akka.http.javadsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.NotFound

final case class NotFoundException(override val msg: String) extends RestEndpointException {
  override val statusCode: StatusCode = NotFound
}
