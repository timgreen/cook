package cook.target

import cook.ref.TargetRef

abstract class Target[R <: TargetResult](
  val ref: TargetRef,
  val buildCmd: TargetBuildCmd[R],
  val resultFn: TargetResultFn[R],
  val metaFn: TargetMetaFn[R],
  val runCmd: Option[TargetRunCmd[R]],
  val deps: List[TargetRef]
) {

}
