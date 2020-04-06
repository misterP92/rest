package sulewski.rest.exceptions

import akka.http.javadsl.model.StatusCode

trait RestEndpointException extends Exception {
  val msg: String
  val statusCode: StatusCode
}
