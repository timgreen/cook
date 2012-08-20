package cook.config.dsl

import cook.config.dsl.buildin.Error
import cook.config.dsl.buildin.GenTarget
import cook.config.dsl.buildin.Glob

trait DslImports extends GenTarget with Glob with Error {

  type TargetResult = cook.target.Target.Result
  type Target[T] = cook.target.Target[T]
  type Context = ConfigContext
  type Path = scala.tools.nsc.io.Path
  type Directory = scala.tools.nsc.io.Directory
}
