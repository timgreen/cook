package cook.path

import cook.util.HashManager

import scala.annotation.tailrec
import scala.tools.nsc.io.Path


/**
 * Path ref.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
class PathRef(val segments: List[String]) {

  def relativePathRefSegments(ref: String): List[String] = {
    PathRef.relative(segments.dropRight(1), ref)
  }

  lazy val p: Path = getPath
  private def getPath = PathUtil().relativeToRoot(segments: _*)
  def hash = HashManager.hash(p)
}

object PathRef {

  def relative(baseSegments: List[String], ref: String): List[String] = {
      if (ref.startsWith("//")) {
        ref.drop(2).split("/").toList
      } else {
        doRelative(baseSegments, ref.split("/").toList)
      }
  }

  @tailrec
  private def doRelative(baseSegments: List[String], segments: List[String]): List[String] = {
    segments match {
      case Nil =>
        baseSegments
      case ".." :: tail =>
        assert(baseSegments.nonEmpty)
        doRelative(baseSegments.drop(1), tail)
      case "." :: tail =>
        doRelative(baseSegments, tail)
      case head :: tail =>
        doRelative(baseSegments :+ head, tail)
    }
  }
}
