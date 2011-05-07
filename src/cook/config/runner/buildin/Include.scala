package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File

import cook.config.runner.ConfigType
import cook.config.runner.CookRunner
import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.unit.RunnableUnitWrapper._
import cook.config.runner.unit._
import cook.config.runner.value._
import cook.util.FileUtil

/**
 * Buildin function include.
 *
 * Example:
 *
 * include("//rules/scala")
 * include("scala")
 * include("subdir/scala")
 */
object Include extends RunnableFuncDef("include", Scope.ROOT_SCOPE, IncludeArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val filename = argsValue("filename").tos
    val pathFromRoot =
        if (filename.startsWith("//")) {
          filename.drop(2) + ".cooki"
        } else {
          path + "/" + filename + ".cooki"
        }

    val cooki = FileUtil(pathFromRoot)
    val scope = CookRunner.run(cooki, ConfigType.cooki)
    // NOTE(timgreen): argsValue.parent is out scope, the caller's scope
    argsValue.parent.merge(scope)

    NullValue()
  }
}

object IncludeArgsDef {

  def apply() = new ArgsDef(Seq[String]("filename"), new HashMap[String, Value])
}
