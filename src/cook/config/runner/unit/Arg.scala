package cook.config.runner.unit

import scala.collection.Seq
import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.value._

import RunnableUnitWrapper._

class ArgsValue(val names: Seq[String], val values: HashMap[String, Value]) {
  // TODO(timgreen):
}

object ArgsValue {

  def apply(args: Seq[Arg], path: String, scope: Scope): ArgsValue = {
    // TODO(timgreen):
    null
  }
}

class ArgsDef(val names: Seq[String], val defaultValues: HashMap[String, Value]) {

}

object ArgsDef {

  def apply(args: Seq[ArgDef], path: String, scope: Scope): ArgsDef = {
    // TODO(timgreen):
    null
  }
}

