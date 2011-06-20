package cook.config.runner.value.methods.string

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._

object EndsWithArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("str")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}

class EndsWith(v: Value) extends ValueMethod(v.name + ".endsWith", v, EndsWithArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val s = v.toStr
    val str = argsValue("str").toStr

    BooleanValue(v.name + ".endsWith(\"" + str + "\")", s.endsWith(str))
  }
}

object EndsWithBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new EndsWith(v)
}
