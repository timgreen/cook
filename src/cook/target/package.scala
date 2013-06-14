package cook

import cook.meta.Meta

package object target {

  trait TargetResult
  type TargetBuildCmd[R <: TargetResult] = Target[R] => Unit
  type TargetResultFn[R <: TargetResult] = Target[R] => R
  type TargetMetaFn[R <: TargetResult] = Target[R] => Meta
  type TargetRunCmd[R <: TargetResult] = (Target[R], List[String]) => Int

  class UnitResult extends TargetResult
  object UnitResult extends UnitResult()
  object UnitResultFn {
    def apply(): TargetResultFn[UnitResult] = { t => UnitResult }
  }

  object EmptyBuildCmd {
    def apply[R <: TargetResult](): TargetBuildCmd[R] = { t => }
  }

  object EmptyMetaFn {
    def apply[R <: TargetResult](): TargetMetaFn[R] = { t => new Meta }
  }
}
