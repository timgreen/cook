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
object AbsPath extends RunnableFuncDef("path", Scope.ROOT_SCOPE, AbsPathArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Value =
      StringValue(FileUtil(path).getAbsolutePath)
}

object AbsPathArgsDef {

  def apply() = new ArgsDef(Seq[String](), new HashMap[String, Value])
}
