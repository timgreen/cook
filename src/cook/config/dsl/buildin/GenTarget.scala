package cook.config.dsl.buildin

import cook.config.dsl.ConfigContext
import cook.target.Target
import cook.target.TargetRef

trait GenTarget {

  def genTarget[T <: Target.Result](
    name: String,
    resultFn: Target.ResultFn[T],
    buildCmd: Target.BuildCmd[T],
    inputMetaFn: Target.InputMetaFn[T],
    runCmd: Option[Target.RunCmd[T]] = None,
    deps: List[TargetRef] = List()
  )(implicit context: ConfigContext): Target[T] = {
    checkTargetName(name)

    val targetRef = new TargetRef(name, context.segments)
    val t = new Target[T](
      ref = targetRef,
      buildCmd = buildCmd,
      resultFn = resultFn,
      inputMetaFn = inputMetaFn,
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
