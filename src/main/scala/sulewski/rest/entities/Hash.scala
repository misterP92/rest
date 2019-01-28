package sulewski.rest.entities

import scala.collection.mutable

case class Hash(code: Long,
                secrets: Iterable[String] = Iterable.empty,
                hint: mutable.HashMap[String, Long] = mutable.HashMap.empty)

object Hash {

}
