package cook.config.dsl

import cook.config.dsl.buildin._

object Dsl extends BuildinCommands {

  val UnitResult    = cook.target.UnitResult
  val UnitResultFn  = cook.target.UnitResultFn
  val EmptyBuildCmd = cook.target.EmptyBuildCmd
}
