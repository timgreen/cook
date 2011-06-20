package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File

import cook.config.runner.EvalException
import cook.config.runner.value._
import cook.util.{Label => UtilLabel}

/**
 * Buildin function labels.
 *
 * return list of label
 *
 * Example:
 *
 * labels("//lib/ant.jar")
 * labels(["//lib/ant.jar", ":target", "File.java"])
 */
object Labels extends BuildinFunction("labels", LabelsArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    var l = argsValue("labels")
    if (l.typeName != "List") {
      l = ListValue(l.name, Seq(l))
    }

    val error = "Buildin function \"labels\" only aceppt string / label or string / label list"
    val labels = l.toListValue(error).map( _ match {
      case StringValue(_, str) => LabelValue("labels()", UtilLabel(path, str))
      case labelValue: LabelValue => labelValue
      case _ => throw new EvalException(error)
    })


    ListValue("labels()", labels)
  }
}

object LabelsArgsDef {

  def apply() = new ArgsDef(Seq[String]("labels"), new HashMap[String, Value])
}
