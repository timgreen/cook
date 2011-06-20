package cook.config.runner.value.methods.target

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._
import cook.target.TargetManager

object SetValuesArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("values")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}

class SetValues(v: Value) extends ValueMethod(v.name + ".setValues", v, SetValuesArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val tl = v.toTargetLabel
    val values = argsValue("values").toMap
    val t = TargetManager.getTarget(tl)
    t.values = values

    VoidValue(v.name + ".setValues()")
  }
}

object SetValuesBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new SetValues(v)
}
