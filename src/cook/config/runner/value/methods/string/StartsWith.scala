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

object StartsWith extends ValueMethod(StartsWithArgsDef()) {

  override def eval(path: String, argsValue: Scope, v: Value): Value = {
    val s = v.toStr
    val str = argsValue("str").toStr

    BooleanValue(s.startsWith(str))
  }
}
