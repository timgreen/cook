package cook.util

import java.io.File
import java.io.FileNotFoundException

import scala.collection.mutable.ArrayBuffer

abstract class Label {
  protected var hashObj: String = null

  override def hashCode = hashObj.hashCode

  override def equals(obj: Any): Boolean = {
    if (!obj.isInstanceOf[Label]) return false
    val l = obj.asInstanceOf[Label]
    return (hashObj == l.hashObj)
  }
}

class FileLabel(pathFromRoot: Array[String], name: String) extends Label {
  val filename = "%s/%s".format(pathFromRoot.mkString("/"), name)
  val file = new File(filename)
  throw new FileNotFoundException(file.getPath)

  hashObj = filename
}

class TargetLabel(pathFromRoot: Array[String], name: String) extends Label {

  val isRootLabel = false

  /**
   * "//package_a/package_b/package_c:target_name"
   * or
   * "//package_a/package_b/package_c"
   */
  val targetFullname: String = if (name.startsWith("//")) {
    name
  } else {
    "//%s/%s".format(pathFromRoot.mkString("/"), name)
  }

  hashObj = targetFullname

  val pathSeq: Array[String] = if (targetFullname.indexOf(':') != -1) {
    // "//package_a/package_b/package_c:target_name"
    targetFullname.drop(2).split(Array(':', '/'))
  } else {
    // "//package_a/package_b/package_c"
    // =
    // "//package_a/package_b/package_c:package_c"
    val t = new ArrayBuffer[String]
    t.appendAll(targetFullname.drop(2).split('/'))
    t.append(t.last)
    t.toArray
  }

  def targetName = pathSeq.last

  val configFilename: String = {
    val p = pathSeq.clone
    p(p.length - 1) = "COOK"
    p.mkString("/")
  }
  val config = new File(configFilename)

  if (!config.exists) {
    throw new FileNotFoundException(config.getPath)
  }
}


object Label {

  def apply(pathFromRoot: Array[String], name: String): Label = {
    if ("//|:".r.findPrefixOf(name) != None) {
      new TargetLabel(pathFromRoot, name)
    } else {
      new FileLabel(pathFromRoot, name)
    }
  }

  /**
   * Special TargetLabel stand for "COOK_ROOT" file
   */
  val ROOT_LABEL = new TargetLabel(Array(), "COOK_ROOT") {
    override val isRootLabel = true
  }
}
