package cook.meta

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.reflect.io.{ Path => SPath }

object FileHash {

  def getHash(p: SPath): String = calcHash(p)

  // TODO(timgreen): use content hash?
  private def calcHash(p: SPath): String =
    if (p.canRead) {
      "%d+%d" format (p.lastModified, p.length)
    } else {
      ""
    }
}
