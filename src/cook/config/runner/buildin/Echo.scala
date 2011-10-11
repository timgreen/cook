package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File

import cook.app.console.CookConsole
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
object Echo extends BuildinFunction("echo", EchoArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val message = argsValue("message").toString
    CookConsole.println(message)

    VoidValue("echo()")
  }
}

object EchoArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("message")
    val defaultValues = new HashMap[String, Value]

    new ArgsDef(names, defaultValues)
  }
}
