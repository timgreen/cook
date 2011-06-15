package cook.config.runner.value

import scala.annotation.tailrec
import scala.collection.mutable.HashMap

class Scope(val values: HashMap[String, Value], val parent: Scope) {

  def merge(toMerge: Scope): Scope = {
    values ++= toMerge.values
    this
  }

  def defineInCurrent(id: String) = values.contains(id)
  def definedInParent(id: String) = (get(parent, id) != None)

  def update(id: String, value: Value) {
    values(id) = value
  }

  def apply(id: String): Value = get(this, id).get
  def get(id: String): Option[Value] = get(this, id)

  @tailrec
  private def get(s: Scope, id: String): Option[Value] = {
    if (s == null) return None
    if (s.values.contains(id)) return s.values.get(id)
    return get(s.parent, id)
  }
}

object Scope {

  def apply(): Scope = apply(ROOT_SCOPE)
  def apply(parent: Scope): Scope = new Scope(new HashMap[String, Value], parent)

  val ROOT_SCOPE: Scope = apply(null)
}
