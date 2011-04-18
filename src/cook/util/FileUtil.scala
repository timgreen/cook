package cook.util

import java.io.File
import java.io.FileNotFoundException

object FileUtil {
  var root: String = null

  val COOK_ROOT_FILENAME = "COOK_ROOT"

  def findRootDir(currentDir: File): File = {
    var dir = currentDir
    do {
      if (isRootDir(dir)) return dir
      dir = dir.getParentFile
    } while (dir != null)

    throw new FileNotFoundException(
        "Can not find Cook Build Root, from dir %s".format(currentDir.getPath))
  }

  private[util]
  def isRootDir(dir: File): Boolean = new File(dir, COOK_ROOT_FILENAME).exists

}
