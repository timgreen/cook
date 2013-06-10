package cook.meta

import java.util.concurrent.{ ConcurrentHashMap => JConcurrentHashMap }
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.reflect.io.{ Path => SPath }

object FileHash {

  private val cache: mutable.ConcurrentMap[String, String] = new JConcurrentHashMap[String, String]

  def getHash(p: SPath): String =
    cache.getOrElseUpdate(p.path, calcHash(p))

    // TODO(timgreen): use content hash?
  private def calcHash(p: SPath): String =
    if (p.canRead) {
      "%d+%d" format (p.lastModified, p.length)
    } else {
      ""
    }
}
