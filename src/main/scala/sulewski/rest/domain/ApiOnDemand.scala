package sulewski.rest.domain
import scala.concurrent.{ExecutionContext, Future}

class ApiOnDemand(implicit ec: ExecutionContext) extends HttpApi[String] {
  override def get(id: String): Future[Option[String]] = getAll.map(_.find(_.equals(id)))

  override def getAll: Future[Seq[String]] = {
    Future.successful(Seq("IT WORKED", "Lorem ipsum"))
  }
}
