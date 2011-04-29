package cook.util

import java.io.File
import java.io.FileNotFoundException

object FileUtil {

  val COOK_ROOT_FILENAME = "COOK_ROOT"

  val COOK_GEN = "cook_gen"
  val COOK_BUILD = "cook_build"

  val OUTPUT_PREFIX = "COOK_TARGET_"
  val LOG_PREFIX = "COOK_TARGET_"

  def findRootDir(): File = findRootDir(new File(System.getProperty("user.dir")))
  def findRootDir(currentDir: File): File = {
    if (root != null) return root

    var dir = currentDir.getAbsoluteFile
    do {
      if (isRootDir(dir)) {
        setRoot(dir)
        return dir
      }
      dir = dir.getParentFile
    } while (dir != null)

    throw new FileNotFoundException(
        "Can not find Cook Build Root, from dir %s".format(currentDir.getPath))
  }

  def apply(filename: String): File = getFileFromRoot(filename)

  def getFileFromRoot(filename: String): File = new File(findRootDir(), filename)

  def getCookRootFile = getFileFromRoot(COOK_ROOT_FILENAME)

  def relativeDirToRoot(filename: String): String = relativeDirToRoot(new File(filename))
  def relativeDirToRoot(file: File): String = {
    val absPath =
        if (file.isDirectory) {
          file.getAbsolutePath
        } else {
          file.getParentFile.getAbsolutePath
        }

    absPath.drop(findRootDir(file).getAbsolutePath.length + 1)
  }

  def getBuildOutputDir(path: String, targetName: String): File = {
    FileUtil("%s/%s/%s%s".format(COOK_BUILD, path, OUTPUT_PREFIX, targetName))
  }

  def getGenerateOutputDir(path: String, targetName: String): File = {
    FileUtil("%s/%s/%s%s".format(COOK_GEN, path, OUTPUT_PREFIX, targetName))
  }

  def getBuildLogFile(path: String, targetName: String): File = {
    FileUtil("%s/%s/%s%s.log".format(COOK_BUILD, path, LOG_PREFIX, targetName))
  }

  def getGenerateLogFile(path: String, targetName: String): File = {
    FileUtil("%s/%s/%s%s.log".format(COOK_GEN, path, LOG_PREFIX, targetName))
  }

  def cookBuildDir = FileUtil(COOK_BUILD)
  def cookGenerateDir = FileUtil(COOK_GEN)

  private[util]
  var root: File = null

  def isRootDir(dir: File): Boolean = new File(dir, COOK_ROOT_FILENAME).exists

  def setRoot(root: File) {
    this.root = root.getAbsoluteFile
    println("COOK_ROOT dir is " + this.root.getAbsolutePath)
  }

}
