package sulewski.rest.entities

case class Email(email: String, domain: String = "gmail.com") {
  override def toString: String = {
    s"$email@$domain"
  }
  def fromString(emailAsString: Option[String]): Option[Email] =
    emailAsString match {
      case Some(emailString) =>
        val parts = emailString.split("@")

        parts.lastOption match {
          case Some(someDomain) =>
            parts.headOption.map(
              Email(_, someDomain)
            )
          case None => None
        }
      case None => None
    }
}
