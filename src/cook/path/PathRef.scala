package cook.path

import cook.util.HashManager

import scala.tools.nsc.io.Path


/**
 * Path ref.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
class PathRef(val segments: List[String]) {

  def relativePathRefSegments(ref: String): List[String] = {
    if (ref.startsWith("//")) {
      ref.drop(2).split("/").toList
    } else {
      segments.take(segments.size - 1) ++ ref.split("/").toList
    }
  }

  lazy val p: Path = getPath
  private def getPath = PathUtil().relativeToRoot(segments: _*)
  def hash = HashManager.hash(p)
}
