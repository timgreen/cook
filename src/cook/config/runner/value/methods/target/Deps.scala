package cook.config.runner.value.methods.target

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._
import cook.target.TargetManager

class Deps(v: Value) extends ValueMethod(v.name + ".deps", v, EmptyArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val tl = v.toTargetLabel
    val t = TargetManager.getTarget(tl)

    ListValue(v.name + ".deps()", t.deps.map(TargetLabelValue("", _)))
  }
}

object DepsBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new Deps(v)
}
