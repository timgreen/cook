package cook

import cook.meta.Meta

package object target {

  trait TargetResult
  type TargetBuildCmd[R <: TargetResult] = Target[R] => Unit
  type TargetResultFn[R <: TargetResult] = Target[R] => R
  type TargetMetaFn[R <: TargetResult] = Target[R] => Meta
  type TargetRunCmd[R <: TargetResult] = (Target[R], List[String]) => Int
}
