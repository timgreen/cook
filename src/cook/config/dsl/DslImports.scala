package cook.config.dsl

import cook.config.dsl.buildin.GenTarget
import cook.config.dsl.buildin.Glob

trait DslImports extends GenTarget with Glob {

  type TargetResult = cook.target.Target.Result
  type Target[T] = cook.target.Target[T]
  type Context = ConfigContext
}
