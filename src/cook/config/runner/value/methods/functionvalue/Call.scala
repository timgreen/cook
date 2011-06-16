package cook.config.runner.value.methods

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._
import cook.config.runner.FunctionValueEvaluator

class Call(argsDef: ArgsDef) extends ValueMethod(argsDef) {

  override def eval(path: String, argsValue: Scope, v: Value): Value = {
    val functionValue = v.asInstanceOf[FunctionValue]
    FunctionValueEvaluator.eval(path, argsValue, functionValue)
  }
}

object CallBuilder extends ValueMethodBuilder {

  override def apply(value: Value): ValueMethod = {
    val functionValue = value.asInstanceOf[FunctionValue]
    new Call(functionValue.argsDef)
  }
}
