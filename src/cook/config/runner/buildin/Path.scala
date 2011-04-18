package cook.config.runner.buildin

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

import java.io.File
import java.io.FilenameFilter

import org.apache.tools.ant.DirectoryScanner

import cook.config.runner.EvalException
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

  override def run(path: String, argsValue: ArgsValue): Option[Value] = Some(StringValue(path))
}

object PathArgsDef {

  def apply() = new ArgsDef(Seq[String](), new HashMap[String, Value])
}
