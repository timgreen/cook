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

class Split(v: Value, name: String) extends ValueMethod(v, name, SplitArgsDef()) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val s = getStringOrError(Some(v))
    val seps = argsValue.get("sep")
    val result =
        seps match {
          case Some(CharValue(sep)) => s.split(sep)
          case _ => s.split(getListCharOrError(seps).toArray)
        }
    ListValue(result.map { StringValue(_) })
  }
}

object Split extends ValueMethodBuilder {

  def apply(v: Value, name: String): ValueMethod = new Split(v, name)
}
