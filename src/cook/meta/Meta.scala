package cook.meta

import scala.collection.mutable

class Meta extends mutable.HashMap[String, String] {

  def add(group: String, key: String, value: String) {
    this(group + "|" + key) = value
  }

  def toBytes: Array[Byte] = Meta.toBytes(this)
}

object Meta {

  def toBytes(meta: Meta): Array[Byte] = {
    // TODO(timgreen):
    null
  }

  def fromBytes(bytes: Array[Byte]): Meta = {
    // TODO(timgreen):
    null
  }
}
