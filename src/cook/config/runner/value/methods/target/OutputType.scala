package cook.config.runner.value.methods.target

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._
import cook.target.TargetManager

class OutputType(v: Value) extends ValueMethod(v.name + ".outputType", v, EmptyArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val tl = v.toTargetLabel
    val t = TargetManager.getTarget(tl)

    StringValue(v.name + ".outputType()", t.outputType)
  }
}

object OutputTypeBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new OutputType(v)
}
