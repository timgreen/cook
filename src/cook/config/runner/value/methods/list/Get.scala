package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._

object GetArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("i")
    val defaultValues = new HashMap[String, Value]

    new ArgsDef(names, defaultValues)
  }
}

class Get(v: ListValue, name: String) extends ValueMethod(v, name, GetArgsDef()) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val i = argsValue("i").toInt
    v.list(i)
  }
}

object Get extends ValueMethodBuilder {

  def apply(v: Value, name: String): ValueMethod =
      new Get(v.asInstanceOf[ListValue], name)
}
