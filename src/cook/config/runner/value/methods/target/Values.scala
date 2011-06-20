package cook.config.runner.value.methods.target

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._
import cook.target.TargetManager

class Values(v: Value) extends ValueMethod(v.name + ".values", v, EmptyArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val tl = v.toTargetLabel
    val t = TargetManager.getTarget(tl)

    MapValue(v.name + ".values()", t.values)
  }
}

object ValuesBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new Values(v)
}
