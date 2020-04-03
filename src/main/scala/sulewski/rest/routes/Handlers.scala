package sulewski.rest.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import sulewski.rest.exceptions.{BadRequestException, ErrorResponse, NotFoundException, RestEndpointException}

object Handlers {
  implicit def rejectionHandle: RejectionHandler =
    RejectionHandler.newBuilder
      .handleNotFound(
        complete(NotFound,
          createErrorResponse(NotFoundException("Path does not exist"))))
      .result()

  implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: BadRequestException =>
        complete(StatusCodes.BadRequest, createErrorResponse(e))
    }

  def createErrorResponse[T <: RestEndpointException](exception: T): ErrorResponse = {
    ErrorResponse(exception.statusCode.intValue().toString,
      exception.getClass.getSimpleName,
      exception.message)
  }

}
