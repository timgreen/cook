package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._

object ContainsArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("substring")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}

object Contains extends ValueMethod(ContainsArgsDef()) {

  override def eval(path: String, argsValue: Scope, v: Value): Value = {
    val s = v.toStr
    val sub = argsValue("substring").toStr

    BooleanValue(s.contains(sub))
  }
}
