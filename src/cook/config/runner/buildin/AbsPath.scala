package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._
import cook.util._

/**
 * Buildin function abspath.
 *
 * return current absolute path: String
 *
 * Example:
 *
 * abspath()
 */
object AbsPath extends RunnableFuncDef("abspath", Scope.ROOT_SCOPE, AbsPathArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    StringValue(new FileLabel(path, argsValue("file").toStr).file.getAbsolutePath)
  }
}

object AbsPathArgsDef {

  def apply() = {
    val names = Seq[String]("file")
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("file", StringValue(""))

    new ArgsDef(names, defaultValues)
  }
}
