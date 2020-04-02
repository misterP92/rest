package sulewski.rest.entities

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

class JsonMarshelling extends SprayJsonSupport with DefaultJsonProtocol {
  //implicit val errorResponseFormat =
  //implicit val notFoundFormat =
  //implicit val identificationFormat:  RootJsonFormat[Identification] = jsonFormat2(Identification)
  //implicit val individualFormat:  RootJsonFormat[Identification]     = jsonFormat8(Individual)
}
