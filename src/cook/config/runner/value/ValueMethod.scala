package cook.config.runner.value

import scala.collection.mutable.HashMap

import cook.config.parser.unit._

object EmptyArgsDef {

  def apply(): ArgsDef = new ArgsDef(Seq[String](), new HashMap[String, Value])
}

abstract class ValueMethod(n: String, val v: Value, argsDef: ArgsDef)
    extends BuildinFunction(v.name + "." + n, argsDef){
}

abstract class ValueMethodBuilder {

  def apply(v: Value): ValueMethod
}

object ValueMethod {

  val stringMethodBuilders = new HashMap[String, ValueMethodBuilder]
  val listMethodBuilders = new HashMap[String, ValueMethodBuilder]
  val targetLabelMethodBuilders = new HashMap[String, ValueMethodBuilder]
  val functionValueMethodBuilders = new HashMap[String, ValueMethodBuilder]
}
