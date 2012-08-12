package cook.util

import cook.error.ErrorMessageHandler

import java.util.concurrent.{ ConcurrentHashMap => JConcurrentHashMap }
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.tools.nsc.io.Path


object HashManager extends ErrorMessageHandler {

  private val cache: mutable.ConcurrentMap[String, String] = new JConcurrentHashMap[String, String]

  def hash(p: Path, force: Boolean = false): String = if (force) {
    val h = calcHash(p)
    cache(p.path) = h
    h
  } else {
    cache.getOrElseUpdate(p.path, calcHash(p))
  }

  // TODO(timgreen): use content hash
  private def calcHash(p: Path): String = {
    "%d|%d" format (p.lastModified, p.length)
  }
}
