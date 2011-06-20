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

class Split(v: Value) extends ValueMethod(v.name + ".split", v, SplitArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val s = v.toStr
    val seps = argsValue("sep")
    val result =
        seps match {
          case CharValue(_, sep) => s.split(sep)
          case _ => s.split(seps.toListChar.toArray)
        }
    ListValue(v.name + ".split()", result.map { StringValue("", _) })
  }
}

object SplitBuilder extends ValueMethodBuilder {

  override def apply(v: Value): ValueMethod = new Split(v)
}
