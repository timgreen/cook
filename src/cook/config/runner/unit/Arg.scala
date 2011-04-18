package cook.config.runner.unit

import scala.collection.Seq
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

import cook.config.parser.unit._
import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.value._

import RunnableUnitWrapper._

class ArgsValue(values: HashMap[String, Value], parent: Scope)
    extends Scope(values, new HashMap[String, RunnableFuncDef], parent)  {
}

object ArgsValue extends RunnableUnit {

  def apply(args: Seq[Arg],
            runnableFuncDef: RunnableFuncDef,
            path: String,
            scope: Scope): ArgsValue = {

    // 1. check wether args match argsDef
    // 2. create ArgsValue map

    val values = new HashMap[String, Value]
    val isNamedList =
        (args.length != runnableFuncDef.argsDef.names.length) ||
        (!args.isEmpty && args.head.isInstanceOf[ArgNamedValue])

    if (isNamedList) {  // Option named list
      values ++= runnableFuncDef.argsDef.defaultValues
      val names = new HashSet[String]
      for (arg <- args) arg match {
        case ArgNamedValue(name, expr) => {
          if (names.contains(name)) {
            throw new EvalException(
                "dulpicated name \"%s\", in named-args func call \"%s\""
                .format(name, runnableFuncDef.name))
          }
          names += name
          val value = getOrError(expr.run(path, scope))
          values.put(name, value)
        }
        case _ => throw new EvalException(
            "name is required in named-args func call \"%s\"".format(runnableFuncDef.name))
      }
    } else {  // Full list
      val nameIter = runnableFuncDef.argsDef.names.iterator
      for (arg <- args) {
        val name = nameIter.next
        val expr = arg match {
          case ArgValue(expr) => expr
          case ArgNamedValue(argName, expr) => {
            if (argName != name) {
              throw new EvalException(
                  ("Wrong order for arg name \"%s\", in fulllist-args func call \"%s\"," +
                    " should be \"%s\"").format(argName, runnableFuncDef.name, name))
            }
            expr
          }
        }
        val value = getOrError(expr.run(path, scope))
        values.put(name, value)
      }

    }

    new ArgsValue(values, runnableFuncDef.scope)
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

