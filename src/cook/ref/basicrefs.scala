package cook.ref

import cook.error.ErrorTracking._
import cook.path.Path

import scala.annotation.tailrec
import scala.tools.nsc.io.{ Path => SPath, Directory }


/**
 * Directory reference.
 *
 * segments is start from cook root.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
class DirRef(val segments: List[String]) extends Ref {

  def toDir: Directory = segments.foldLeft(Path().rootDir: SPath)(_ / _).toDirectory
  override def refName: String = segments.mkString("", "/", "/")
}

object DirRefFactory extends RefFactory[DirRef] {

  /**
   * DirRef must endsWith '/' and not contains ":", "//".
   *
   * StartsWith '/' means abs path, e.g
   *   "/src/a/b/c/"
   *   "/"
   * Otherwise means relative path, e.g
   *   "a/b/c/"
   *   "d"
   * "." and ".." is valid, e.g
   *   "../../../a/b/c"
   *   "./x/y/z"
   * But the ref should never jump out of the cook root.
   *   "/../../a/" is invalid
   */
  override def apply(baseSegments: List[String], refName: String): Option[DirRef] = {
    if (refName.lastOption != Some('/') || refName.indexOf("//") > 0 || refName.indexOf(":") > 0) {
      None
    } else {
      val segments =
        if (refName.headOption == Some('/')) {
          relativeDir(Nil, refName.drop(1).split('/').toList)
        } else {
          relativeDir(baseSegments, refName.split('/').toList)
        }
      Some(new DirRef(segments))
    }
  }

  @tailrec
  private def relativeDir(baseSegments: List[String], segments: List[String]): List[String] = {
    segments match {
      case Nil =>
        baseSegments
      case ".." :: tail =>
        if (baseSegments.isEmpty) {
          reportError("Bad ref, dirref can not jump out of cook root")
        }
        relativeDir(baseSegments.dropRight(1), tail)
      case "." :: tail =>
        relativeDir(baseSegments, tail)
      case head :: tail =>
        relativeDir(baseSegments :+ head, tail)
    }
  }
}

abstract class PathRef(val dir: DirRef, lastPart: String) extends Ref

class FileRef(dir: DirRef, val filename: String) extends PathRef(dir, filename) {

  def toPath: SPath = dir.toDir / filename
  override def refName: String =  dir.refName + "/" + filename
}

object FileRefFactory extends RefFactory[FileRef] {

  val P = "(.*/)?([^/]+)".r
  override def apply(baseSegments: List[String], refName: String): Option[FileRef] = {
    refName match {
      case P(refDir, filename) =>
        DirRefFactory(baseSegments, Option(refDir).getOrElse("")) map { dir =>
          new FileRef(dir, filename)
        }
      case _ => None
    }
  }
}

class TargetRef(dir: DirRef, val targetName: String) extends PathRef(dir, targetName) {

  override def refName: String =  dir.refName + ":" + targetName
  def cookFileRef: FileRef = new FileRef(dir, "COOK")
}

object TargetRefFactory extends RefFactory[TargetRef] {

  val P = "(.*):([^:/]+)".r
  override def apply(baseSegments: List[String], refName: String): Option[TargetRef] = {
    refName match {
      case P(refDir, targetName) =>
        DirRefFactory(baseSegments, refDir) map { dir =>
          new TargetRef(dir, targetName)
        }
      case _ => None
    }
  }
}

// TODO(timgreen): Plugin Target ref
// Start with //<plugin_name>/
// e.g. //mvn/com.github.scopt%%scopt%2.1.0"
// can be generated by some helper function like:
// mvn / "com.github.scopt" %% "scopt" % "2.1.0"
trait PluginTargetRef extends Ref

trait PluginTargetRefFactory[P <: PluginTargetRef] extends RefFactory[P] {

  val pluginName: String

  override def apply(baseSegments: List[String], refName: String): Option[P] = {
    val prefix = "//" + pluginName + "/"
    if (refName.startsWith(prefix)) {
      parseRefName(refName.drop(prefix.size))
    } else {
      None
    }
  }

  def parseRefName(refName: String): Option[P] = None
}
