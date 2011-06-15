package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File

import org.apache.tools.ant.DirectoryScanner

import cook.config.runner.EvalException
import cook.config.runner.value._
import cook.util._

/**
 * Buildin function glob.
 *
 * return filelist: ListValue
 *
 * Example:
 *
 * glob(["*.java"])
 */
object Glob extends BuildinFunction(GlobArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    var l = argsValue("filters")
    if (l.typeName != "List") {
      l = ListValue(Seq(l))
    }
    val filters = l.toListStr("Buildin function \"glob\" only aceppt string or string list")

    val dirScanner = new DirectoryScanner
    dirScanner.setIncludes(filters.toArray)
    dirScanner.setBasedir(FileUtil(path))
    dirScanner.scan

    val files = dirScanner.getIncludedFiles

    ListValue(files.map((f) => {
      FileLabelValue(new FileLabel(path, f))
    }))
  }
}

object GlobArgsDef {

  def apply() = new ArgsDef(Seq[String]("filters"), new HashMap[String, Value])
}
