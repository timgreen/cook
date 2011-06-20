package cook.config.runner.value.methods.string

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

class Contains(v: Value) extends ValueMethod(v.name + ".contains", v, ContainsArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val s = v.toStr
    val sub = argsValue("substring").toStr

    BooleanValue(v.name + ".contains(\" + sub + \")", s.contains(sub))
  }
}

object ContainsBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new Contains(v)
}
