package cook.util

import java.io.File
import java.io.FileNotFoundException

import cook.util._

abstract class Label {
  protected var hashObj: String = null

  override def hashCode = hashObj.hashCode

  override def equals(obj: Any): Boolean = {
    if (!obj.isInstanceOf[Label]) return false
    val l = obj.asInstanceOf[Label]
    return (hashObj == l.hashObj)
  }
}

class FileLabel(pathFromRoot: String, name: String) extends Label {

  /**
   * "//package_a/package_b/package_c/filename"
   * or
   * "filename"
   */
  val filename =
      if (name.startsWith("//")) {
        name.drop(2)
      } else {
        "%s/%s".format(pathFromRoot, name)
      }
  val file = FileUtil(filename)

  if (!file.exists) {
    throw new FileNotFoundException(file.getPath)
  }

  hashObj = "F" + filename
}

class TargetLabel(pathFromRoot: String, name: String) extends Label {

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
        "%s%s".format(pathFromRoot, name)
      }

  if (targetName.indexOf(':') == -1) {
    throw new CookBaseException("Target name must contain \":\", invalid name \"%s\"", targetName)
  }

  val configFilename: String = targetName.split(":")(0) + "/COOK"
  val config = FileUtil(configFilename)

  if (!config.exists) {
    throw new FileNotFoundException(config.getPath)
  }

  hashObj = "T" + configFilename
}
