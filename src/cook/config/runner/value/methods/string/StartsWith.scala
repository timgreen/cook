package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._

object StartsWithArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("str")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}

class StartsWith(v: StringValue, name: String) extends ValueMethod(v, name, StartsWithArgsDef()) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val s = v.str
    val str = argsValue("str").toStr

    BooleanValue(s.startsWith(str))
  }
}

object StartsWith extends ValueMethodBuilder {

  def apply(v: Value, name: String): ValueMethod =
      new StartsWith(v.asInstanceOf[StringValue], name)
}
