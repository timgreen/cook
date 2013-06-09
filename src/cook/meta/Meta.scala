package cook.meta

import scala.collection.mutable

class Meta extends mutable.HashMap[String, String] {

  def add(group: String, key: String, value: String) {
    this(group + "|" + key) = value
  }
}

object Meta {

  def apply = new Meta
}
