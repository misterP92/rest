package sulewski.rest.exceptions

import akka.http.javadsl.model.StatusCode

trait RestEndpointException extends Exception {
  val message: String
  val statusCode: StatusCode
}
