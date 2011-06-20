package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._

object StartsWithArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("str")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}

class StartsWith(v: Value) extends ValueMethod(v.name + ".startsWith", v, StartsWithArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val s = v.toStr
    val str = argsValue("str").toStr

    BooleanValue(v.name + ".startsWith(\"" + str + "\")", s.startsWith(str))
  }
}

object StartsWithBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new StartsWith(v)
}
