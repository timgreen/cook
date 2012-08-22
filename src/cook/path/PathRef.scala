package cook.path

import cook.error.ErrorTracking._
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

  def refName = segments.mkString("/", "/", "")
}

object PathRef {

  def relative(baseSegments: List[String], ref: String): List[String] = {
    val segments = if (ref.startsWith("/")) {
      ref.drop(1).split("/").toList
    } else {
      doRelative(baseSegments, ref.split("/").toList)
    }
    for (seg <- segments) {
      if (seg.isEmpty) {
        reportError("Bad ref: %s", ref)
      }
    }
    segments
  }

  @tailrec
  private def doRelative(baseSegments: List[String], segments: List[String]): List[String] = {
    segments match {
      case Nil =>
        baseSegments
      case ".." :: tail =>
        if (baseSegments.isEmpty) {
          reportError("Bad ref")
        }
        doRelative(baseSegments.drop(1), tail)
      case "." :: tail =>
        doRelative(baseSegments, tail)
      case head :: tail =>
        doRelative(baseSegments :+ head, tail)
    }
  }
}
