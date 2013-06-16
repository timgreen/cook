package cook.config.dsl

import cook.config.dsl.buildin._

object Dsl extends BuildinCommands with Implicits {

  type TargetResult = cook.target.TargetResult
  type Target[R <: TargetResult] = cook.target.Target[R]
  type UnitResult = cook.target.UnitResult

  val UnitResult    = cook.target.UnitResult
  val UnitResultFn  = cook.target.UnitResultFn
  val EmptyBuildCmd = cook.target.EmptyBuildCmd
  val EmptyMetaFn   = cook.target.EmptyMetaFn
}
