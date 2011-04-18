package cook.config.runner.unit

import scala.collection.Seq
import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.value._

import RunnableUnitWrapper._

class ArgsValue(values: HashMap[String, Value], parent: Scope)
    extends Scope(values, new HashMap[String, RunnableFuncDef], parent)  {
}

object ArgsValue {

  def apply(args: Seq[Arg],
            runnableFuncDef: RunnableFuncDef,
            path: String,
            scope: Scope): ArgsValue = {

    // 1. check wether args match argsDef
    // 2. create ArgsValue map
    // TODO(timgreen):


    null
  }
}

class ArgsDef(val names: Seq[String], val defaultValues: HashMap[String, Value]) {

}

object ArgsDef extends RunnableUnit {

  def apply(args: Seq[ArgDef], path: String, scope: Scope): ArgsDef = {
    val defaultValues = new HashMap[String, Value]
    val names = args.map {
      _ match {
        case ArgDefName(name) => name
        case ArgDefNameValue(name, expr) => {
          val value = getOrError(expr.run(path, scope))
          defaultValues.put(name, value)
          name
        }
      }
    }

    new ArgsDef(names, defaultValues)
  }
}

