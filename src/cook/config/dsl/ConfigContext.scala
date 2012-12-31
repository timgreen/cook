package cook.config.dsl

import cook.ref.DirRef
import cook.ref.FileRef
import cook.target.Target

import scala.collection.mutable

class ConfigContext(val configRef: FileRef) {

  private [dsl] val targets = mutable.Map[String, Target[_]]()

  def dir = configRef.dir
  def targets: List[Target[_]] = targets.values

  private [dsl] def addTarget[T](t: Target[T]) {
    targets +: (t.targetName -> t)
  }
}
