package cook.config.dsl

trait DslImports extends GenTarget {

  type TargetResult = cook.target.Target.Result
  type Target[T <: TargetResult] = cook.target.Target[T]
  type Context = ConfigContext
}
