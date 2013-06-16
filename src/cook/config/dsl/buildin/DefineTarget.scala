package cook.config.dsl.buildin

import cook.config.dsl.ConfigContext
import cook.ref.{Ref, TargetRef, NativeTargetRef}
import cook.target.{Target, NativeTarget, TargetResult, TargetResultFn, TargetBuildCmd, TargetMetaFn, TargetRunCmd}

trait DefineTarget {

  def defineTarget[T <: TargetResult](
    name: String,
    resultFn: TargetResultFn[T],
    buildCmd: TargetBuildCmd[T],
    inputMetaFn: TargetMetaFn[T],
    runCmd: Option[TargetRunCmd[T]] = None,
    deps: Seq[Ref] = Nil
  )(implicit context: ConfigContext): NativeTarget[T] = {
    require(resultFn    != null, "resultFn can not be null")
    require(buildCmd    != null, "buildCmd can not be null")
    require(inputMetaFn != null, "inputMetaFn can not be null")
    require(runCmd      != null, "runCmd can not be null")
    require(deps        != null, "deps can not be null")
    require(deps.forall(_.isTarget), "dep must be TargetRef")
    require(passTargetNameCheck(name), "invalid target name: " + name)
    val targetDeps = deps map { _.as[TargetRef] }

    val targetRef = new NativeTargetRef(context.dir, name)
    val t = new NativeTarget[T](
      ref = targetRef,
      buildCmd = buildCmd,
      resultFn = resultFn,
      inputMetaFn = inputMetaFn,
      runCmd = runCmd,
      deps = targetDeps
    )

    context addTarget t

    t
  }

  private def passTargetNameCheck(name: String): Boolean = {
    name.matches("^[a-zA-Z0-9_]+$")
  }
}
