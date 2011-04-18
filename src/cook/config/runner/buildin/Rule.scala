package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File
import java.io.FilenameFilter

import org.apache.tools.ant.DirectoryScanner

import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._
import cook.target.Target
import cook.target.Targets

/**
 * Buildin function rule.
 *
 * return none
 *
 * Example:
 *
 * rule(
 *     name = "testTarget",
 *     path = path,
 *     input = [ "a" ],
 *     output = [ "b" ],
 *     cmd = "cat a > b"
 * )
 */
class Rule extends RunnableFuncDef("rule", Scope.ROOT_SCOPE, RuleArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Option[Value] = {
    // create rule "path:name" and store it

    val name = getStringOrError(argsValue.get("name"))
    val cmd = getStringOrError(argsValue.get("cmd"))
    val basePath = argsValue.get("path") match {
      case Some(StringValue(str)) => str
      case Some(NullValue()) => path
      case _ => throw new EvalException("param \"path\" should be StringValue")
    }
    val input = getListStringOrError(argsValue.get("input"))
    val output = getListStringOrError(argsValue.get("ouptut"))
    val deps = getListStringOrError(argsValue.get("deps"))
    val exeCmd = argsValue.get("exeCmd") match {
      case Some(StringValue(str)) => str
      case _ => null
    }

    Targets.push(new Target(name, basePath, input, output, deps, exeCmd))

    None
  }
}

object RuleArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("name", "input", "output", "cmd", "exeCmd", "deps")
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("input", ListValue())
    defaultValues.put("output", ListValue())
    defaultValues.put("exeCmd", NullValue())
    defaultValues.put("deps", ListValue())
    defaultValues.put("path", NullValue())

    new ArgsDef(names, defaultValues)
  }
}
