package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._

object JoinArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("sep")
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("sep", StringValue(","))

    new ArgsDef(names, defaultValues)
  }
}

object Join extends ValueMethod(JoinArgsDef()) {

  override def eval(path: String, argsValue: Scope, v: Value): Value = {
    val list = v.toListStr
    val sep = argsValue("sep").toStr

    StringValue(list.mkString(sep))
  }
}
