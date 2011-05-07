package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._

object ContainsArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("substring")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}

class Contains(v: Value, name: String) extends ValueMethod(v, name, ContainsArgsDef()) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val s = v.tos
    val sub = argsValue("substring").tos

    BooleanValue(s.contains(sub))
  }
}

object Contains extends ValueMethodBuilder {

  def apply(v: Value, name: String): ValueMethod = new Contains(v, name)
}
