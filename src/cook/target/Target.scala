package cook.target

import cook.error.ErrorTracking._
import cook.ref.TargetRef


object TargetStatus extends Enumeration {
  type TargetStatus = Value
  val Pending, Cached, Built = Value
}


abstract class Target[+R <: TargetResult](
    val ref: TargetRef,
    private[this] val buildCmd: TargetBuildCmd[R],
    private[this] val resultFn: TargetResultFn[R],
    private[this] val metaFn: TargetMetaFn[R],
    private[this] val runCmd: Option[TargetRunCmd[R]],
    val deps: Seq[TargetRef]) {

  def refName = ref.refName

  import TargetStatus._
  private var _status: TargetStatus = Pending
  def status = _status
  def isResultReady = (_status == Cached) || (_status == Built)

  // TODO(timgreen): use meta
  private def needBuild: Boolean = (_status == Pending)

  private[this] var _result: Option[R] = None
  def result: R = _result getOrElse {
    if (!isResultReady) {
      reportError("Can not call target %s.result, target not built yet. Do you miss deps", refName)
    }

    val r = resultFn(this)
    _result = Some(r)
    r
  }

  private [cook] def build {
    if (needBuild) {
      ref.targetBuildDir.createDirectory(force = true)
      buildCmd(this)
      _status = Built
    }
  }

  private [cook] def run(args: List[String] = List()): Int = runCmd match {
    case None =>
      reportError("Can not run target %s, target not runable", refName)
    case Some(cmd) =>
      if (needBuild) {
        reportError("Can not run target %s, target not built yet. Do you miss deps", refName)
      }
      cmd(this, args)
  }

}
