package cook.config.dsl

import cook.config.dsl.buildin.Error
import cook.config.dsl.buildin.GenTarget
import cook.config.dsl.buildin.Glob
import cook.config.dsl.buildin.Meta
import cook.config.dsl.buildin.Refs

trait DslImports extends GenTarget with Glob with Error with Refs with Meta {

  type Context = ConfigContext

  type Target[T] = cook.target.Target[T]
  type TargetResult = cook.target.Target.Result
  type UnitResult = cook.target.Target.UnitResult
  def unitResultFn[T] = cook.target.Target.unitResultFn[T]

  val EmptyBuildCmd = cook.target.Target.EmptyBuildCmd
  val EmptyInputMetaFn = cook.target.Target.EmptyInputMetaFn

  type Path = scala.tools.nsc.io.Path
  type Directory = scala.tools.nsc.io.Directory

  type RefType = cook.config.dsl.buildin.RefType.RefType
  val RefTypePath = cook.config.dsl.buildin.RefType.Path
  val RefTypeTarget = cook.config.dsl.buildin.RefType.Target
  val RefTypeUnknown = cook.config.dsl.buildin.RefType.Unknown
}
