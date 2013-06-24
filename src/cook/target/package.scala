package cook

import cook.meta.Meta

package object target {

  trait TargetResult {
    def as[R <: TargetResult]: R = this.asInstanceOf[R]
  }

  type TargetAndResult = (Target[TargetResult], TargetResult)
  type TargetBuildCmd[R <: TargetResult] = (Target[R], List[Target[TargetResult]]) => Unit
  type TargetResultFn[R <: TargetResult] = (Target[R], List[Target[TargetResult]]) => R
  type TargetMetaFn[R <: TargetResult] = Target[R] => Meta
  type TargetRunCmd[R <: TargetResult] = (Target[R], List[String]) => Int

  class UnitResult extends TargetResult
  object UnitResult extends UnitResult()
  object UnitResultFn {
    def apply(): TargetResultFn[UnitResult] = { (t, deps) => UnitResult }
  }

  object EmptyBuildCmd {
    def apply[R <: TargetResult](): TargetBuildCmd[R] = { (t, deps) => }
  }

  object EmptyMetaFn {
    def apply[R <: TargetResult](): TargetMetaFn[R] = { t => new Meta }
  }
}
