package cook.meta

import scala.collection.mutable

class Meta extends mutable.HashMap[String, String] {

  def add(group: String, key: String, value: String) {
    this(group + "." + key) = value
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

  def withPrefix(prefix: String): Meta = {
    val m = new Meta
    m ++= this map { case (k, v) =>
      s"${prefix}.${k}" -> v
    }
    m
  }
}

object Meta {

  import java.io._

  def toBytes(meta: Meta): Array[Byte] = {
    val ba = new ByteArrayOutputStream
    val out = new ObjectOutputStream(ba)
    out.writeObject(meta)
    out.close
    ba.toByteArray
  }

  def fromBytes(bytes: Array[Byte]): Meta = {
    try {
      val in =
        new ObjectInputStream(new ByteArrayInputStream(bytes))
      in.readObject().asInstanceOf[Meta]
    } catch {
      case e: Throwable =>
        new Meta
    }
  }
}
