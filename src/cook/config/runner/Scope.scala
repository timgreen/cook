package cook.config.runner

import scala.collection.mutable.HashMap

import cook.config.parser.unit._

class Scope(val vars: HashMap[String, Expr], val funcs: HashMap[String, FuncDef]) {
  def this() {
    this(new HashMap[String, Expr], new HashMap[String, FuncDef])
  }

  override def clone() = new Scope(vars.clone, funcs.clone)

}

object Scope {
  def apply() = new Scope
}
