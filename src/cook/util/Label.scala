package cook.util

import java.io.File
import java.io.FileNotFoundException

import scala.collection.mutable.ArrayBuffer

abstract class Label

case class FileLabel(val pathFromRoot: Array[String], val name: String) extends Label {
  val file = new File(pathFromRoot.mkString, name)
  throw new FileNotFoundException(file.getPath)
}

case class TargetLabel(val pathFromRoot: Array[String], val name: String) extends Label {

  val isRootLabel = false

  /**
   * "//package_a/package_b/package_c:target_name"
   */
  val targetFullname: String = if (name.startsWith("//")) {
    name
  } else {
    "//%s/%s".format(pathFromRoot.mkString("/"), name)
  }

  val pathSeq: Array[String] = if (targetFullname.indexOf(':') != -1) {
    targetFullname.drop(2).split(Array(':', '/'))
  } else {
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
