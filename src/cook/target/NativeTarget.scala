package cook.target

import cook.error.ErrorTracking._
import cook.ref.TargetRef

class NativeTarget[R <: TargetResult](
  ref: TargetRef,
  buildCmd: TargetBuildCmd[R],
  resultFn: TargetResultFn[R],
  metaFn: TargetMetaFn[R],
  runCmd: Option[TargetRunCmd[R]],
  deps: List[TargetRef]) extends Target[R](ref, buildCmd, resultFn, metaFn, runCmd, deps) {

  override protected def isCached: Boolean = {
    // TODO(timgreen)
    val meta = metaFn(this)
    false
  }

}
