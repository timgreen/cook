package cook.config.dsl

import cook.target.Target
import cook.target.TargetRef

trait GenTarget {

  def genTarget[T](
    name: String,
    buildCmd: Target.BuildCmd[T],
    resultFn: Target.ResultFn[T],
    inputMetaFn: Target.InputMetaFn[T],
    runCmd: Option[Target.RunCmd[T]] = None,
    deps: List[String] = List()
  )(implicit context: ConfigContext): Target[T] = {
    checkTargetName(name)

    val targetRef = TargetRef(name, context.segments)
    val t = new Target(
      ref = targetRef,
      buildCmd = buildCmd,
      resultFn = resultFn,
      inputMetaFn = inputMetaFn,
      runCmd = runCmd,
      deps = deps map targetRef.relativeTargetRef
    )

    context addTarget t

    t
  }

  private def checkTargetName(name: String) {
    // TODO(timgreen):
  }
}
