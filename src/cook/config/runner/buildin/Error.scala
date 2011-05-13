package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._
import cook.util._

/**
 * Buildin function error.
 *
 * Example:
 *
 * error("error message")
 */
object Error extends RunnableFuncDef("error", Scope.ROOT_SCOPE, ErrorArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val message = argsValue("message").toStr
    throw new EvalException(message)
  }
}

object ErrorArgsDef {

  def apply() = {
    val names = Seq[String]("message")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}
