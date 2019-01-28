package sulewski.rest.entities

case class Individual(acronym: String,
                      email: Email,
                      name: Option[String] = None,
                      surname: Option[String] = None,
                      `mobile-number`: Option[String] = None,
                      characteristics: Map[String, String] = Map.empty,
                      subscriptions: Iterable[Subscription] = Iterable.empty,
                      `my-identification`: Option[Identification] = None)