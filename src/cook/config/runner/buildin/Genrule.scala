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
 * Buildin function genrule.
 *
 * return none
 *
 * Example:
 *
 * genrule(
 *     name = "testTarget",
 *     path = path,
 *     input = [ "a" ],
 *     cmds = [ "cat $INPUTS > b" ]
 * )
 */
object Genrule extends RunnableFuncDef("genrule", Scope.ROOT_SCOPE, GenruleArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    // create rule "path:name" and store it

    val name = argsValue("name").tos
    val cmds = getListStringOrError(argsValue.get("cmds"))
    val inputs = getListStringOrError(argsValue.get("inputs"))
    val tools = getListStringOrError(argsValue.get("tools"))
    val deps = getListStringOrError(argsValue.get("deps"))
    val exeCmds =
        try {
          getListStringOrError(argsValue.get("exeCmds"))
        } catch {
          case _ => null  // ignore
        }
    val isGenerateTarget = getBoolOrError(argsValue.get("isGenerateTarget"))

    var targetName = "%s:%s".format(path, name)
    TargetManager.push(
        new Target(path, name, cmds, inputs, deps, tools, exeCmds, isGenerateTarget))

    NullValue()
  }
}

object GenruleArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("name", "inputs", "cmds", "exeCmds", "deps", "tools", "isGenerateTarget")
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("inputs", ListValue())
    defaultValues.put("exeCmds", NullValue())
    defaultValues.put("deps", ListValue())
    defaultValues.put("tools", ListValue())
    defaultValues.put("isGenerateTarget", BooleanValue.FALSE)

    new ArgsDef(names, defaultValues)
  }
}
