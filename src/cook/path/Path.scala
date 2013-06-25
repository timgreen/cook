package cook.path

import cook.console.ops._
import cook.error._

import scala.annotation.tailrec
import scala.reflect.io.{ Path => SPath, Directory }

class Path(val rootDir: Directory, val currentDir: Directory) {

  val cookWorkspaceDir =
    (rootDir / ".cook_workspace").toDirectory

  val configScalaSourceDir =
    (cookWorkspaceDir / "config" / "srcs").toDirectory
  val configByteCodeDir =
    (cookWorkspaceDir / "config" / "bytecodes").toDirectory
  val targetBuildDir =
    (cookWorkspaceDir / "targets").toDirectory
  val metaDir =
    (cookWorkspaceDir / "metas").toDirectory
  val logDir =
    (cookWorkspaceDir / "logs").toDirectory

  val currentSegments = currentDir.segments.drop(rootDir.segments.size)
}

object Path {

  private var instance: Path = _

  def apply(): Path = instance
  private [cook] def apply(currentDir: Option[Directory]): Path = {
    currentDir.map(_.normalize.toDirectory).flatMap(d => findRootDir(d, d)) match {
      case Some((currentDir, rootDir)) =>
        instance = new Path(rootDir, currentDir)
        instance
      case None =>
        reportError {
          "Can not find Cook Root Dir, from dir %s" :: strong(currentDir.toString)
        }
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
