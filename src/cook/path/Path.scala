package cook.path

import cook.error.ErrorTracking._

import scala.annotation.tailrec
import scala.reflect.io.{ Path => SPath, Directory }

class Path(val rootDir: Directory, val currentDir: Directory) {

  val WORKSPACE = ".cook_workspace"

  val configScalaSourceDir =
    (rootDir / WORKSPACE / "config_srcs").toDirectory
  val configByteCodeDir =
    (rootDir / WORKSPACE / "config_bytecodes").toDirectory
  val targetBuildDir =
    (rootDir / WORKSPACE / "targets").toDirectory
  val configMetaDir =
    (rootDir / WORKSPACE / "config_metas").toDirectory

  val currentSegments = currentDir.segments.drop(rootDir.segments.size)
}

object Path {

  private var instance: Path = _

  def apply(): Path = instance
  def apply(currentDir: Option[Directory]): Path = {
    currentDir.map(_.normalize.toDirectory).flatMap(d => findRootDir(d, d)) match {
      case Some((currentDir, rootDir)) =>
        instance = new Path(rootDir, currentDir)
        instance
      case None =>
        reportError("Can not find Cook Root Dir, from dir %s", currentDir.toString)
    }
  }

  @tailrec
  private def findRootDir(dir: Directory, currentDir: Directory): Option[(Directory, Directory)] = {
    if (isCookRoot(dir)) {
      Some(currentDir -> dir.toAbsolute)
    } else if (dir.isRootPath) {
      None
    } else {
      findRootDir(dir.parent, currentDir)
    }
  }

  private def isCookRoot(dir: Directory) = (dir / "COOK_ROOT").canRead
}
