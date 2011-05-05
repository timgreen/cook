package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._

abstract class ValueMethod(val v: Value, name: String, argsDef: ArgsDef)
    extends RunnableFuncDef(name, Scope.ROOT_SCOPE, argsDef, null, null) {
}

trait ValueMethodBuilder {
  def apply(v: Value, name: String): ValueMethod
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

  val stringMethods = new HashMap[String, ValueMethodBuilder]
  val listMethods = new HashMap[String, ValueMethodBuilder]

  private[value]
  def getMethod(
      methods: HashMap[String, ValueMethodBuilder], v: Value, name: String): ValueMethod = {

    methods.get(name) match {
      case Some(m) => m(v, name)
      case None => error(v, name)
    }
  }
}
