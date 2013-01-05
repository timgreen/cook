package cook.target

import cook.config.ConfigManager
import cook.ref.NativeTargetRef
import cook.ref.PluginTargetRef
import cook.ref.TargetRef

object TargetManager {

  def getTarget(ref: TargetRef): Target[TargetResult] = ref match {
    case nativeTargetRef: NativeTargetRef =>
      ConfigManager.load(nativeTargetRef.cookFileRef).getTarget(nativeTargetRef.targetName)
    case _: PluginTargetRef =>
      // TODO(timgreen):
      null
  }
}
