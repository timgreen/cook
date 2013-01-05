package cook.config

import cook.config.dsl.ConfigContext
import cook.ref.DirRef
import cook.target.Target
import cook.target.TargetResult

trait Config {

  implicit val context: ConfigContext
  implicit def provideDirRef: DirRef = context.dir

  def getTargetOption(targetName: String): Option[Target[TargetResult]] =
    context.targets.get(targetName)
  def getTarget(targetName: String): Target[TargetResult] = context.targets(targetName)
}
