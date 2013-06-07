package cook.config.dsl.buildin

import cook.config.dsl.ConfigContext
import cook.ref.{TargetRef, NativeTargetRef}
import cook.target.{Target, NativeTarget, TargetResult, TargetResultFn, TargetBuildCmd, TargetMetaFn, TargetRunCmd}

trait DefineTarget {

  def defineTarget[T <: TargetResult](
    name: String,
    resultFn: TargetResultFn[T],
    buildCmd: TargetBuildCmd[T],
    metaFn: TargetMetaFn[T],
    runCmd: Option[TargetRunCmd[T]] = None,
    deps: List[TargetRef] = Nil
  )(implicit context: ConfigContext): NativeTarget[T] = {
    checkTargetName(name)

    val targetRef = new NativeTargetRef(context.dir, name)
    val t = new NativeTarget[T](
      ref = targetRef,
      buildCmd = buildCmd,
      resultFn = resultFn,
      metaFn = metaFn,
      runCmd = runCmd,
      deps = deps
    )

    context addTarget t

    t
  }

  private def checkTargetName(name: String) {
    // TODO(timgreen):
  }
}
