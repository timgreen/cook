package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File

import cook.config.runner.EvalException
import cook.config.runner.value._
import cook.util.{Label => UtilLabel}

/**
 * Buildin function label.
 *
 * return label
 *
 * Example:
 *
 * label("//lib/ant.jar")
 * label(":target")
 * label("File.java")
 */
object Label extends BuildinFunction("label", LabelArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    var label = argsValue("label").toStr
    LabelValue("label()", UtilLabel(path, label))
  }
}

object LabelArgsDef {

  def apply() = new ArgsDef(Seq[String]("label"), new HashMap[String, Value])
}
