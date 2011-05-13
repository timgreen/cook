package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File

import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._
import cook.util._

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
object Labels extends RunnableFuncDef("labels", Scope.ROOT_SCOPE, LabelsArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    var l= argsValue("labels")
    if (l.typeName != "List") {
      l= ListValue(Seq(l))
    }

    val error = "Buildin function \"label\" only aceppt string / label or string / label list"
    val labels = l.toListValue(error).map( _ match {
      case StringValue(str) => {
        Label(path, str) match {
          case fileLabel: FileLabel => FileLabelValue(fileLabel)
          case targetLabel: TargetLabel => TargetLabelValue(targetLabel)
        }
      }
      case fileLabelValue: FileLabelValue => fileLabelValue
      case targetLabelValue: TargetLabelValue => targetLabelValue
      case _ => throw new EvalException(error)
    })


    ListValue(labels)
  }
}

object LabelsArgsDef {

  def apply() = new ArgsDef(Seq[String]("labels"), new HashMap[String, Value])
}
