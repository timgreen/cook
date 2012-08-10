package cook.path

import cook.error.ErrorMessageHandler

import scala.annotation.tailrec
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


object PathUtil extends ErrorMessageHandler {

  private var cookRootOption: Option[Directory] = None
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

  lazy val cookRoot = cookRootOption.get
  lazy val cookBuildDir = (cookRoot / ".cook_build").toDirectory
  lazy val cookConfigScalaSourceDir = (cookBuildDir / "config_srcs").toDirectory
  lazy val cookConfigClassDir = (cookBuildDir / "config_classes").toDirectory
  lazy val cookTargetBuildDir = (cookBuildDir / "targets").toDirectory

  def relativeToRoot(path: Path): List[String] = {
    cookRoot.relativize(path).segments
  }
  def relativeToRoot(segments: String*): Path = {
    segments.foldLeft(cookRoot: Path)(_ / _)
  }
}
