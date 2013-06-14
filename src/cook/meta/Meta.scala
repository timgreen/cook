package cook.meta

import scala.collection.mutable

class Meta extends mutable.HashMap[String, String] {

  def add(group: String, key: String, value: String) {
    this(group + "|" + key) = value
  }

  def merge(other: Meta) =
    this ++= other

  def + (other: Meta): Meta = (new Meta).merge(this).merge(other)

  def toBytes: Array[Byte] = Meta.toBytes(this)

  def hash: String = {
    val h = this map { v =>
      v._1.hashCode + v._2.hashCode
    } sum

    Math.abs(h).toString
  }
}

object Meta {

  // TODO(timgreen): re-impl toBytes & fromBytes

  def toBytes(meta: Meta): Array[Byte] = {
    meta.mkString("\n").getBytes("UTF-8")
  }

  def fromBytes(bytes: Array[Byte]): Meta = {
    val m = new Meta
    try {
      val s = new String(bytes)
      val values = s.split('\n') map { line =>
        val Array(key, value) = line.split(" -> ", 2)
        key -> value
      }
      m ++= values
    } catch {
      case e: Throwable =>
    }
    m
  }
}
