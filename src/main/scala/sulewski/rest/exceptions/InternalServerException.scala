package sulewski.rest.exceptions

import akka.http.javadsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.InternalServerError

final case class InternalServerException(override val msg: String) extends RestEndpointException {
  override val statusCode: StatusCode = InternalServerError
}
