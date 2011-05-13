package cook.util

import java.io.File
import java.io.FileNotFoundException

object FileUtil {

  val COOK_ROOT_FILENAME = "COOK_ROOT"

  val COOK_BUILD = "cook_build"

  val OUTPUT_PREFIX = "COOK_TARGET_BUILD_"
  val BUILD_LOG_PREFIX = "COOK_TARGET_BUILD_"
  val CACHE_PREFIX = "COOK_TARGET_BUILD_"

  val RUN_LOG_PREFIX = "COOK_TARGET_RUN_"

  def findRootDir(currentDir: File): File = {
    if (root != null) return root

    var dir = currentDir.getAbsoluteFile
    do {
      if (isRootDir(dir)) {
        return dir
      }
      dir = dir.getParentFile
    } while (dir != null)

    throw new FileNotFoundException(
        "Can not find Cook Build Root, from dir %s".format(currentDir.getPath))
  }

  def apply(filename: String): File = getFileFromRoot(filename)

  def getFileFromRoot(filename: String): File = new File(root, filename)

  def getCookRootFile = getFileFromRoot(COOK_ROOT_FILENAME)

  def relativeDirToRoot(filename: String): String = relativeDirToRoot(new File(filename))
  def relativeDirToRoot(file: File): String = {
    val absPath =
        if (file.isDirectory) {
          file.getAbsolutePath
        } else {
          file.getParentFile.getAbsolutePath
        }

    absPath.drop(root.getAbsolutePath.length + 1)
  }

  def getBuildOutputDir(path: String, targetName: String): File = {
    FileUtil("%s/%s/%s%s".format(COOK_BUILD, path, OUTPUT_PREFIX, targetName))
  }

  def getBuildLogFile(path: String, targetName: String): File = {
    FileUtil("%s/%s/%s%s.log".format(COOK_BUILD, path, BUILD_LOG_PREFIX, targetName))
  }

  def getBuildCacheMetaFile(path: String, targetName: String): File = {
    FileUtil("%s/%s/%s%s.cachemeta".format(COOK_BUILD, path, CACHE_PREFIX, targetName))
  }

  def getRunLogFile(path: String, targetName: String): File = {
    FileUtil("%s/%s/%s%s.log".format(COOK_BUILD, path, RUN_LOG_PREFIX, targetName))
  }

  def cookBuildDir = FileUtil(COOK_BUILD)

  def setRoot(root: File) {
    this.root = root.getAbsoluteFile
  }

  private[util]
  var root: File = null

  def isRootDir(dir: File): Boolean = new File(dir, COOK_ROOT_FILENAME).exists

}
