package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import cook.config.runner.value._
import cook.error.ErrorMessageHandler
import cook.util._

/**
 * Buildin function error.
 *
 * Example:
 *
 * error("error message")
 */
object Error extends BuildinFunction("error", ErrorArgsDef()) with ErrorMessageHandler {

  override def eval(path: String, argsValue: Scope): Value = {
    val message = argsValue("message").toStr
    reportError(message)
  }
}

object ErrorArgsDef {

  def apply() = {
    val names = Seq[String]("message")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}
