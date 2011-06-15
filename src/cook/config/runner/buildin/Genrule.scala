package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import java.io.File

import cook.config.runner.EvalException
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
object Genrule extends BuildinFunction(GenruleArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    // create rule "path:name" and store it

    val name = argsValue("name").toStr
    val cmds = argsValue("cmds").toListStr
    val inputs = argsValue("inputs").toListFileLabel
    val deps = argsValue("deps").toListTargetLabel
    val exeCmds = argsValue("exeCmds").toListStr

    var targetName = "%s:%s".format(path, name)
    TargetManager.push(
        new Target(path, name, cmds, inputs, deps, exeCmds))

    NullValue()
  }
}

object GenruleArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("name", "inputs", "cmds", "exeCmds", "deps")
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("inputs", ListValue())
    defaultValues.put("exeCmds", ListValue())
    defaultValues.put("deps", ListValue())

    new ArgsDef(names, defaultValues)
  }
}
