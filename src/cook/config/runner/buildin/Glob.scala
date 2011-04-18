package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File

import org.apache.tools.ant.DirectoryScanner

import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._

/**
 * Buildin function glob.
 *
 * return filelist: ListValue
 *
 * Example:
 *
 * glob(["*.java"])
 */
class Glob extends RunnableFuncDef("glob", Scope.ROOT_SCOPE, GlobArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val filters = getOrError(argsValue.get("filters")) match {
      case ListValue(list) => list.map {
        _ match {
          case StringValue(str) => str
          case _ => throw new EvalException("Buildin function only aceppt string list")
        }
      }
      case _ => throw new EvalException("Buildin function only aceppt string list")
    }

    val dirScanner = new DirectoryScanner
    dirScanner.setIncludes(filters.toArray)
    dirScanner.setBasedir(path)
    dirScanner.scan

    val files = dirScanner.getIncludedFiles

    ListValue(files.map { StringValue(_) })
  }
}

object GlobArgsDef {

  def apply() = new ArgsDef(Seq[String]("filters"), new HashMap[String, Value])
}
