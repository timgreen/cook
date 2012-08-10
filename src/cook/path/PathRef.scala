package cook.path

import cook.util.HashManager
import cook.util.PathUtil

import scala.tools.nsc.io.Path


/**
 * Path ref.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
class PathRef(val segments: List[String]) {

  def relativePathRefParts(ref: String): List[String] = {
    if (ref.startsWith("//")) {
      ref.drop(2).split("/").toList
    } else {
      segments.take(segments.size - 1) ++ ref.split("/").toList
    }
  }

  lazy val p: Path = segments.foldLeft(PathUtil.cookRoot: Path)(_ / _)
  def hash = HashManager.hash(p)
}
