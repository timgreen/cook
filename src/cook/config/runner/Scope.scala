package cook.config.runner

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.value._

class Scope(val vars: HashMap[String, Value],
            val funcs: HashMap[String, FuncDef],
            val parent: Scope) {

  def newChildScope = Scope(this)

  def definedInParent(id: String) = (get(parent, id) != None)

  def get(id: String): Option[Value] = get(this, id)

  private def get(s: Scope, id: String): Option[Value] = {
    if (s == null) return None
    if (s.vars.contains(id)) return s.vars.get(id)
    return get(s.parent, id)
  }

  def funcDefinedInParent(id: String) = (getFunc(parent, id) != None)

  def getFunc(id: String): Option[FuncDef] = getFunc(this, id)

  private def getFunc(s: Scope, id: String): Option[FuncDef] = {
    if (s == null) return None
    if (s.funcs.contains(id)) return s.funcs.get(id)
    return getFunc(s.parent, id)
  }
}

object Scope {

  def apply(): Scope = apply(ROOT_SCOPE)

  def apply(parent: Scope): Scope =
      new Scope(new HashMap[String, Value], new HashMap[String, FuncDef], parent)

  val ROOT_SCOPE = apply(null)
}
