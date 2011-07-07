package cook.util

import java.io.File

object DeleteUtil {

  def deleteRecursively(f: File): Boolean = {
    if (!isSymlink(f) && f.isDirectory) f.listFiles match {
      case null =>
      case xs   => xs foreach deleteRecursively
    }
    f.delete
  }

  private def isSymlink(f: File): Boolean = {
    f.getCanonicalPath != f.getAbsolutePath
  }
}
