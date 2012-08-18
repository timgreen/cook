package cook.config.dsl

import cook.config.dsl.buildin.GenTarget

trait DslImports extends GenTarget {

  type TargetResult = cook.target.Target.Result
  type Target[T] = cook.target.Target[T]
  type Context = ConfigContext
}
