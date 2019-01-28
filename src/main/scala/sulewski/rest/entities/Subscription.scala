package sulewski.rest.entities

import java.time.LocalDateTime

case class Subscription(id: String,
                        hash: Hash,
                        `web-space`: Option[String] = None,
                        `subscription-type`: Option[String] = None,
                        `register-date`: Option[LocalDateTime] = None,
                        characteristics: Map[String, String] = Map.empty)

