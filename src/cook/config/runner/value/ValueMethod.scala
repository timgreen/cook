package cook.config.runner.value

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.EvalException

abstract class ValueMethod(val argsDef: ArgsDef) {

  def eval(path: String, argsValue: Scope, v: Value): Value
}

object ValueMethod {

  def apply(v: Value, name: String): ValueMethod = v match {
    case _: StringValue => getMethod(stringMethods, v, name)
    case _: ListValue => getMethod(listMethods, v, name)
    case _ => error(v, name)
  }

  def error(v: Value, name: String) = {
    throw new EvalException("Unsupportted method call \"%s\" on %s", name, v.typeName)
  }

  val stringMethods = new HashMap[String, ValueMethod]
  val listMethods = new HashMap[String, ValueMethod]

  private[value]
  def getMethod(
      methods: HashMap[String, ValueMethod], v: Value, name: String): ValueMethod = {

    methods.get(name) match {
      case Some(m) => m
      case None => error(v, name)
    }
  }
}
