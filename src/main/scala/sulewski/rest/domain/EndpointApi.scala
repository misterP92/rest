package sulewski.rest.domain

import sulewski.rest.entities.Endpoints

import scala.concurrent.Future

class EndpointApi {

  def get(id: String): Future[Option[Endpoints]] = Future.successful(Option.empty)

  def getAll: Future[Iterable[Endpoints]] = {

    Future.successful(Iterable.empty)
  }
}
