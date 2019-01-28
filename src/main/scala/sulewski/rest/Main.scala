package sulewski.rest

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging

object Main extends StrictLogging {
  def main(args: Array[String]): Unit = {
    logger.info(s"Starting running the server")

    implicit val system: ActorSystem = ActorSystem("pattes-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

  }
}
