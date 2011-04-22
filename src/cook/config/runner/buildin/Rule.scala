package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File

import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._
import cook.target.Target
import cook.target.TargetManager
import cook.util.FileUtil

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

  override def run(path: String, argsValue: ArgsValue): Value = {
    // create rule "path:name" and store it

    val name = getStringOrError(argsValue.get("name"))
    val cmds = getListStringOrError(argsValue.get("cmds"))
    val inputs = getListStringOrError(argsValue.get("inputs")).map { FileUtil(_) }
    val deps = getListStringOrError(argsValue.get("deps"))
    val exeCmds =
        try {
          getListStringOrError(argsValue.get("exeCmds"))
        } catch {
          case _ => null  // ignore
        }

    var fullname = "%s:%s".format(path, name)
    println("Create target \"%s\"".format(fullname))
    TargetManager.push(new Target(path, name, cmds, inputs, deps, exeCmds))

    NullValue()
  }
}

object RuleArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("name", "inputs", "cmds", "exeCmds", "deps")
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("inputs", ListValue())
    defaultValues.put("exeCmds", NullValue())
    defaultValues.put("deps", ListValue())

    new ArgsDef(names, defaultValues)
  }
}
