package cook.target

import cook.error.ErrorTracking._
import cook.ref.TargetRef

abstract class Target[+R <: TargetResult](
  val ref: TargetRef,
  private[this] val buildCmd: TargetBuildCmd[R],
  private[this] val resultFn: TargetResultFn[R],
  private[this] val metaFn: TargetMetaFn[R],
  private[this] val runCmd: Option[TargetRunCmd[R]],
  val deps: List[TargetRef]
) {
  def refName = ref.refName

  // TODO(timgreen): use meta
  protected def isCached: Boolean

  private[this] var _result: Option[R] = _
  private var built: Boolean = false
  private def needBuild: Boolean = !built && !isCached

  def result: R = _result getOrElse {
    if (needBuild) {
      reportError("Can not call target %s.result, target not built yet. Do you miss deps",
        ref.refName)
    }

    val r = resultFn(this)
    _result = Some(r)
    r
  }

  private [cook] def build {
    if (needBuild) {
      ref.targetBuildDir.createDirectory(force = true)
      buildCmd(this)
      built = true
    }
  }

  private [cook] def run(args: List[String] = List()): Int = runCmd match {
    case None =>
      reportError("Can not run target %s, target not runable", ref.refName)
    case Some(cmd) =>
      if (needBuild) {
        reportError("Can not run target %s, target not built yet. Do you miss deps", ref.refName)
      }
      cmd(this, args)
  }

}
