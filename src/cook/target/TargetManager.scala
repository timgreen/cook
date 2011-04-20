package cook.target

import scala.collection.mutable.HashMap

object TargetManager {

  val targets = new HashMap[String, Target]

  def push(t: Target) {
    targets.put(t.name, t)
  }

  def hasTarget(name: String) = targets.contains(name)

}
