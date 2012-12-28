package cook.util

import java.io.File

import cook.error.ErrorMessageHandler

abstract class Label {
  protected var hashObj: String = null

  override def hashCode = hashObj.hashCode

  override def equals(obj: Any): Boolean = {
    if (!obj.isInstanceOf[Label]) return false
    val l = obj.asInstanceOf[Label]
    return (hashObj == l.hashObj)
  }
}

object Label {

  def apply(pathFromRoot: String, name: String): Label = {
    if (name.indexOf(':') != -1) {
      new TargetLabel(pathFromRoot, name)
    } else {
      new FileLabel(pathFromRoot, name)
    }
  }
}

class FileLabel(pathFromRoot: String, name: String) extends Label with ErrorMessageHandler {

  /**
   * "//package_a/package_b/package_c/filename"
   * or
   * "/abspath/filename"
   * or
   * "filename"
   */
  val file =
      if (name.startsWith("//")) {
        FileUtil(name.drop(2))
      } else if (name.startsWith("/")) {
        new File(name)
      } else {
        FileUtil("%s/%s".format(pathFromRoot, name))
      }

  hashObj = "F" + file.getAbsolutePath
}

class TargetLabel(pathFromRoot: String, name: String) extends Label with ErrorMessageHandler {

  /**
   * "//package_a/package_b/package_c:target_name"
   * or
   * "package_c:target_name"
   * or
   * ":target_name"
   */
  val targetName =
      if (name.startsWith("//")) {
        name.drop(2)
      } else {
        val sep =
            if (name.startsWith(":") || pathFromRoot.isEmpty) {
              ""
            } else {
              "/"
            }
        "%s%s%s".format(pathFromRoot, sep, name)
      }

  if (targetName.indexOf(':') == -1) {
    reportError("Target name must contain \":\", invalid name \"%s\"", targetName)
  }

  val configFilename: String = {
    val f = targetName.split(":")(0) + "/COOK"
    if (f.startsWith("/")) {
      f.drop(1)
    } else {
      f
    }
  }
  val config = FileUtil(configFilename)

  if (!config.exists) {
    reportError("COOK config not found: %s", config.getPath)
  }

  def outputDir: File = {
    val pathAndName = targetName.split(":")
    FileUtil.getBuildOutputDir(pathAndName(0), pathAndName(1))
  }

  hashObj = "T" + configFilename
}
