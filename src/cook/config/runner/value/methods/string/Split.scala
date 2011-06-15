package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._

object SplitArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("sep")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}

object Split extends ValueMethod(SplitArgsDef()) {

  override def eval(path: String, argsValue: Scope, v: Value): Value = {
    val s = v.toStr
    val seps = argsValue("sep")
    val result =
        seps match {
          case CharValue(sep) => s.split(sep)
          case _ => s.split(seps.toListChar.toArray)
        }
    ListValue(result.map { StringValue(_) })
  }
}
