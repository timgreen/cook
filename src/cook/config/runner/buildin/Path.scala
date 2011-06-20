package cook.config.runner.buildin

import scala.collection.mutable.HashMap

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
object Path extends BuildinFunction("path", PathArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = StringValue("path()", path)
}

object PathArgsDef {

  def apply() = new ArgsDef(Seq[String](), new HashMap[String, Value])
}
