package sulewski.rest.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import sulewski.rest.exceptions._

trait Handlers {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit def rejectionHandle: RejectionHandler =
    RejectionHandler.newBuilder
      .handleNotFound(
        complete(NotFound,
          createErrorResponse(NotFoundException("Path does not exist"))))
      .result()

  implicit def exceptionHandle: ExceptionHandler =
    ExceptionHandler {
      case e: NotFoundException => complete(StatusCodes.NotFound, createErrorResponse(e))
      case e: BadRequestException => complete(StatusCodes.BadRequest, createErrorResponse(e))
      case e: InternalServerException => complete(StatusCodes.InternalServerError, createErrorResponse(e))
      case e => complete(StatusCodes.InternalServerError, createErrorResponse(InternalServerException(e.getMessage)))
    }

  def createErrorResponse[T <: RestEndpointException](exception: T): io.circe.Json = {
    ErrorResponse(exception.statusCode.intValue().toString,
      exception.getClass.getSimpleName,
      exception.msg).toJson
  }

}
