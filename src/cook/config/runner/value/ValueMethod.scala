package cook.config.runner.value

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.EvalException

abstract class ValueMethod(val argsDef: ArgsDef) {

  def eval(path: String, argsValue: Scope, v: Value): Value
}

abstract class ValueMethodBuilder {

  def apply(v: Value): ValueMethod
}

object ValueMethod {

  def apply(v: Value, name: String): ValueMethod = v match {
    case _: StringValue => getMethod(stringMethodBuilders, v, name)
    case _: ListValue => getMethod(listMethodBuilders, v, name)
    case _: FunctionValue => getMethod(functionValueMethodBuilders, v, name)
    case _ => error(v, name)
  }

  def error(v: Value, name: String) = {
    throw new EvalException("Unsupportted method call \"%s\" on %s", name, v.typeName)
  }

  val stringMethodBuilders = new HashMap[String, ValueMethodBuilder]
  val listMethodBuilders = new HashMap[String, ValueMethodBuilder]
  val functionValueMethodBuilders = new HashMap[String, ValueMethodBuilder]

  private[value]
  def getMethod(
      methodBuilders: HashMap[String, ValueMethodBuilder], v: Value, name: String): ValueMethod = {

    methodBuilders.get(name) match {
      case Some(builder) => builder(v)
      case None => error(v, name)
    }
  }
}
