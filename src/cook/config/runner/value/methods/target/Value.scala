package cook.config.runner.value.methods.target

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._
import cook.target.TargetManager

object ValueArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("key")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}

class AttachedValue(v: Value) extends ValueMethod(v.name + ".value", v, ValueArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val tl = v.toTargetLabel
    val key = argsValue("key").toStr
    val t = TargetManager.getTarget(tl)

    val value = t.values(key)
    value.name = v.name + ".value(\"" + key + "\")"
    value
  }
}

object ValueBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new AttachedValue(v)
}
