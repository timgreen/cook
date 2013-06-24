package cook.config.dsl

trait TypeAndValues {
  // path
  type Path                      = scala.reflect.io.Path
  type Directory                 = scala.reflect.io.Directory

  // context
  type Context                   = cook.config.dsl.ConfigContext

  // refs
  type Ref                       = cook.ref.Ref
  type PathRef                   = cook.ref.PathRef
  type FileRef                   = cook.ref.FileRef
  type DirRef                    = cook.ref.DirRef
  type TargetRef                 = cook.ref.TargetRef
  type NativeTargetRef           = cook.ref.NativeTargetRef
  type PluginTargetRef           = cook.ref.PluginTargetRef

  // target
  type TargetResult              = cook.target.TargetResult
  type Target[R <: TargetResult] = cook.target.Target[R]
  type TargetAndResult           = cook.target.TargetAndResult
  type UnitResult                = cook.target.UnitResult

  val UnitResult                 = cook.target.UnitResult
  val UnitResultFn               = cook.target.UnitResultFn
  val EmptyBuildCmd              = cook.target.EmptyBuildCmd
  val EmptyMetaFn                = cook.target.EmptyMetaFn
}
