package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._

object GetArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("i")
    val defaultValues = new HashMap[String, Value]

    new ArgsDef(names, defaultValues)
  }
}

class Get(v: Value) extends ValueMethod(v.name + ".get", v, GetArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val i = argsValue("i").toInt
    val value = v.toListValue("")(i)
    value.name = v.name + "[" + i + "]"
    value
  }
}

object GetBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new Get(v)
}
