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
object Genrule extends BuildinFunction("genrule", GenruleArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    // create rule "path:name" and store it

    val name = argsValue("name").toStr
    val outputType = argsValue("outputType").toStr
    val cmds = argsValue("cmds").toListStr
    val inputs = argsValue("inputs").toListFileLabel
    val deps = argsValue("deps").toListTargetLabel
    val exeCmds = argsValue("exeCmds").toListStr
    val preBuild = argsValue("preBuild").toFuntionValue
    val postBuild = argsValue("postBuild").toFuntionValue
    val preRun = argsValue("preRun").toFuntionValue

    val targetName = "%s:%s".format(path, name)

    if ((preBuild != null) && (cmds != null)) {
      throw new EvalException(
          "Found error in genrule(%s), \"preBuild\" return value will override \"cmds\"",
          targetName)
    }

    if ((preRun != null) && (exeCmds != null)) {
      throw new EvalException(
          "Found error in genrule(%s), \"preBuild\" return value will override \"cmds\"",
          targetName)
    }

    checkFunction(preBuild, "preBuild", targetName)
    checkFunction(postBuild, "postBuild", targetName)
    checkFunction(preRun, "preRun", targetName)

    TargetManager.push(new Target(
        path, name, outputType, cmds, inputs, deps, exeCmds, preBuild, postBuild, preRun))

    VoidValue("genrule(" + targetName + ")")
  }

  private def checkFunction(functionValue: FunctionValue, name: String, targetName: String) {
    if (functionValue == null) {
      return
    }

    if (functionValue.argsDef.names.length != 1) {
      throw new EvalException(
          "Param \"%s\" for genrule(%s) should only take one arg: t: TargetLabel", name, targetName)
    }
  }
}

object GenruleArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String](
        "name",
        "outputType",
        "cmds",
        "inputs",
        "deps",
        "exeCmds",
        "preBuild",
        "postBuild",
        "preRun")
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("inputs",    ListValue("inputs"))
    defaultValues.put("deps",      ListValue("deps"))
    defaultValues.put("exeCmds",   ListValue("exeCmds"))
    defaultValues.put("preBuild",  NullValue("preBuild"))
    defaultValues.put("postBuild", NullValue("postBuild"))
    defaultValues.put("preRun",    NullValue("preRun"))

    new ArgsDef(names, defaultValues)
  }
}
