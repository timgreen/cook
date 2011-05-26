package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File

import cook.app.console.CookConsole
import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._
import cook.target.Target
import cook.target.TargetManager
import cook.util.FileUtil

/**
 * Buildin function echo.
 *
 * return none
 *
 * Example:
 *
 * echo("hello")
 */
object Echo extends RunnableFuncDef("echo", Scope.ROOT_SCOPE, EchoArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val message = argsValue("message").toString
    CookConsole.println(message)

    NullValue()
  }
}

object EchoArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("message")
    val defaultValues = new HashMap[String, Value]

    new ArgsDef(names, defaultValues)
  }
}
