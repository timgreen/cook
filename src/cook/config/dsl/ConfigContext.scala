package cook.config.dsl

import cook.ref.DirRef
import cook.ref.FileRef
import cook.target.Target
import cook.target.TargetResult

import scala.collection.mutable

class ConfigContext(val configRef: FileRef) {

  private [config] val targets = mutable.Map[String, Target[TargetResult]]()
  def dir = configRef.dir

  private [dsl] def addTarget(t: Target[TargetResult]) {
    targets += (t.ref.targetName -> t)
  }
}
