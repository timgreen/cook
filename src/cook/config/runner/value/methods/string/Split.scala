package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._

object SplitArgsDef {

  def apply(): ArgsDef = {
    val names = Seq[String]("sep")
    val defaultValues = new HashMap[String, Value]
    new ArgsDef(names, defaultValues)
  }
}

class Split(v: StringValue, name: String)
    extends ValueMethod(v, name, SplitArgsDef()) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val s = v.str
    val seps = argsValue("sep")
    val result =
        seps match {
          case CharValue(sep) => s.split(sep)
          case _ => s.split(seps.toListChar.toArray)
        }
    ListValue(result.map { StringValue(_) })
  }
}

object Split extends ValueMethodBuilder {

  def apply(v: Value, name: String): ValueMethod =
      new Split(v.asInstanceOf[StringValue], name)
}
