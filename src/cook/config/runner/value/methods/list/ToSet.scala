package cook.config.runner.value.methods.list

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._

object ToSetArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]()
    val defaultValues = new HashMap[String, Value]

    new ArgsDef(names, defaultValues)
  }
}

class ToSet(v: Value) extends ValueMethod(v.name + ".toSet", v, ToSetArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val value = v.toListValue("").toSet.toList
    ListValue(v.name + ".toSet()", value)
  }
}

object ToSetBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new ToSet(v)
}
