package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._

object EndsWithArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("str")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}

class EndsWith(v: StringValue, name: String) extends ValueMethod(v, name, EndsWithArgsDef()) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val s = v.str
    val str = argsValue("str").toStr

    BooleanValue(s.endsWith(str))
  }
}

object EndsWith extends ValueMethodBuilder {

  def apply(v: Value, name: String): ValueMethod =
      new EndsWith(v.asInstanceOf[StringValue], name)
}
