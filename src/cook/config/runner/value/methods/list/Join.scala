package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._

object JoinArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("sep")
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("sep", StringValue(","))

    new ArgsDef(names, defaultValues)
  }
}

class Join(v: Value, name: String) extends ValueMethod(v, name, JoinArgsDef()) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val list = getListStringOrError(Some(v))
    val sep = getStringOrError(argsValue.get("sep"))

    StringValue(list.mkString(sep))
  }
}

object Join extends ValueMethodBuilder {

  def apply(v: Value, name: String): ValueMethod = new Join(v, name)
}
