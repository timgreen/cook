package cook.path

import cook.error.ErrorMessageHandler

import scala.annotation.tailrec
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


class PathUtil(
  private var cookRootOption: Option[Directory] = None,
  cookBuildDirOption: Option[Directory] = None,
  cookConfigScalaSourceDirOption: Option[Directory] = None,
  cookConfigClassDirOption: Option[Directory] = None,
  cookTargetBuildDirOption: Option[Directory] = None
) extends ErrorMessageHandler {

  def findRootDir(currentDir: Directory): Directory = cookRootOption match {
    case Some(r) => r
    case None =>
      cookRootOption = findRootDirInternal(currentDir)
      if (!cookRootOption.isDefined) {
        reportError("Can not find Cook Build Root, from dir %s".format(currentDir.toString))
      }
      cookRootOption.get
  }

  @tailrec
  private def findRootDirInternal(dir: Directory): Option[Directory] = if (isCookRoot(dir)) {
    Some(dir.toAbsolute)
  } else if (dir.isRootPath) {
    None
  } else {
    findRootDirInternal(dir.parent)
  }

  private def isCookRoot(dir: Directory) = (dir / "COOK_ROOT").canRead

  private def getDir(opt: Option[Directory], d: => Directory) = {
    (opt getOrElse d) toAbsolute
  }

  lazy val cookRoot = cookRootOption.get toAbsolute
  lazy val cookBuildDir =
    getDir(cookBuildDirOption, (cookRoot / ".cook_build").toDirectory)
  lazy val cookConfigScalaSourceDir =
    getDir(cookConfigScalaSourceDirOption, (cookBuildDir / "config_srcs").toDirectory)
  lazy val cookConfigClassDir =
    getDir(cookConfigClassDirOption, (cookBuildDir / "config_classes").toDirectory)
  lazy val cookTargetBuildDir =
    getDir(cookTargetBuildDirOption, (cookBuildDir / "targets").toDirectory)

  def relativeToRoot(path: Path): List[String] = {
    cookRoot.relativize(path).segments
  }
  def relativeToRoot(segments: String*): Path = {
    segments.foldLeft(cookRoot: Path)(_ / _)
  }
}

object PathUtil {
  private [path] var instance: PathUtil = new PathUtil()
  def apply() = instance
}
