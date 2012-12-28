package cook.config.dsl

import cook.config.ConfigRef
import cook.path.PathRef
import cook.path.PathUtil
import cook.target.Target

import scala.collection.mutable

class ConfigContext(val ref: ConfigRef) {

  private var targetList = mutable.ArrayBuffer[Target[_]]()

  def targets: List[Target[_]] = targetList.toList

  lazy val path = ref.parentPath.toDirectory
  lazy val segments = PathUtil().relativeToRoot(path)

  private [dsl] def addTarget[T](t: Target[T]) {
    targetList += t
  }
}
