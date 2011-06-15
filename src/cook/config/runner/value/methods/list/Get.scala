package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._

object GetArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("i")
    val defaultValues = new HashMap[String, Value]

    new ArgsDef(names, defaultValues)
  }
}

object Get extends ValueMethod(GetArgsDef()) {

  override def eval(path: String, argsValue: Scope, v: Value): Value = {
    val i = argsValue("i").toInt
    v.toListValue("")(i)
  }
}
