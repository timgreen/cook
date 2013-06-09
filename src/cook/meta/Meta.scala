package cook.meta

import scala.collection.mutable

class Meta extends mutable.HashMap[String, String] {

  def add(group: String, key: String, value: String) {
    this(group + "|" + key) = value
  }

  def toBytes: Array[Byte] = Meta.toBytes(this)
}

object Meta {

  // TODO(timgreen): re-impl toBytes & fromBytes

  def toBytes(meta: Meta): Array[Byte] = {
    meta.mkString("\n").getBytes("UTF-8")
  }

  def fromBytes(bytes: Array[Byte]): Meta = {
    val s = new String(bytes)
    val values = s.split('\n') map { line =>
      val Array(key, value) = line.split(" -> ", 2)
      key -> value
    }
    new Meta  ++= values
  }
}
