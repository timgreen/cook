package cook.config.dsl

import cook.config.ConfigRef
import cook.path.PathRef
import cook.target.Target

import scala.collection.mutable

class ConfigContext(val ref: ConfigRef) {

  private var targetList = mutable.ArrayBuffer[Target]()

  def targets: List[Target] = targetList.toList

  def path = ref.parentPath

  private [dsl] def addTarget(t: Target) {
    targetList += t
  }
}
