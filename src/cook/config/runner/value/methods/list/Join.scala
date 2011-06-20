package cook.config.runner.value.methods.list

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._

object JoinArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("sep")
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("sep", StringValue("sep", ","))

    new ArgsDef(names, defaultValues)
  }
}

class Join(v: Value) extends ValueMethod(v.name + ".join", v, JoinArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val list = v.toListStr
    val sep = argsValue("sep").toStr

    StringValue(v.name + ".join(\""+ sep + "\")", list.mkString(sep))
  }
}

object JoinBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new Join(v)
}
