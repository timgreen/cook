package cook.config.runner.value

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.EvalException

abstract class ValueMethod {

  def eval(v: Value, args: Seq[Arg]): Value
}

object ValueMethod {

  def stringCall(v: Value, name: String, args: Seq[Arg]) = call(stringMethods, v, name, args)
  def listCall(v: Value, name: String, args: Seq[Arg]) = call(listMethods, v, name, args)

  def error(v: Value, name: String): Value = {
    throw new EvalException("Unsupportted method call \"%s\" on %s", name, v.typeName)
  }

  val stringMethods = new HashMap[String, ValueMethod]
  val listMethods = new HashMap[String, ValueMethod]

  private[value]
  def call(methods: HashMap[String, ValueMethod], v: Value, name: String, args: Seq[Arg]): Value = {
    methods.get(name) match {
      case Some(m) => m.eval(v, args)
      case None => error(v, name)
    }
  }
}
