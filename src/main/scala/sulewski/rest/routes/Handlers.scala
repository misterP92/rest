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
          createErrorResponse(NotFoundException("Path does not exist")).toJson))
      .result()

  implicit def exceptionHandle: ExceptionHandler =
    ExceptionHandler {
      case e: NotFoundException =>
        println(e.msg)
        complete(StatusCodes.NotFound, createErrorResponse(e).toJson)
      case e: BadRequestException =>
        complete(StatusCodes.BadRequest, createErrorResponse(e).toJson)
      case e: InternalServerException =>
        complete(StatusCodes.InternalServerError, createErrorResponse(e).toJson)
    }

  def createErrorResponse[T <: RestEndpointException](exception: T): ErrorResponse = {
    ErrorResponse(exception.statusCode.intValue().toString,
      exception.getClass.getSimpleName,
      exception.msg)
  }

}
