package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._

/**
 * Buildin function path.
 *
 * return current path: String
 *
 * Example:
 *
 * path()
 */
class Path extends RunnableFuncDef("path", Scope.ROOT_SCOPE, PathArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Value = StringValue(path)
}

object PathArgsDef {

  def apply() = new ArgsDef(Seq[String](), new HashMap[String, Value])
}
