package cook.config.runner.buildin

import scala.collection.mutable.HashMap

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
object AbsPath extends BuildinFunction("abspath", AbsPathArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    StringValue("abspath()", new FileLabel(path, argsValue("file").toStr).file.getAbsolutePath)
  }
}

object AbsPathArgsDef {

  def apply() = {
    val names = Seq[String]("file")
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("file", StringValue("file", ""))

    new ArgsDef(names, defaultValues)
  }
}
