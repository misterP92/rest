package sulewski.rest.exceptions

final case class ErrorResponse(status: String, code: String, detail: String) extends Exception
