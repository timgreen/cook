package cook.target

import cook.app.Stage
import cook.error.ErrorTracking._

import scala.tools.nsc.io.Path


class Target[T](
  val ref: TargetRef,
  val buildCmd: Target.BuildCmd[T],
  val resultFn: Target.ResultFn[T],
  val inputMetaFn: Target.InputMetaFn[T],
  val runCmd: Option[Target.RunCmd[T]],
  val deps: List[TargetRef]
) extends TargetMeta {

  private var _result: Option[T] = None
  private var built: Boolean = false
  private lazy val userInputMeta = inputMetaFn(this)
  private lazy val isMetaNotChanged = checkIfMetaChanged

  def result: T = {
    if (!Stage.isTargetResultReady) {
      reportError("<target>.result not avaliable in stage: '%s'", Stage.stage.toString)
    }
    assert(!needBuild, "when assign target.result, target must be built")
    _result match {
      case Some(r) => r
      case None =>
        val r = resultFn(this)
        _result = Some(r)
        r
    }
  }

  def result_=(r: T) {
    _result = Some(r)
  }

  def needBuild = !built && isMetaNotChanged

  private[cook] def build {
    if (needBuild) {
      buildCmd(this)
      built = true
    }
  }

  def buildDir = ref.buildDir
  def runDir = ref.runDir

  def refName = ref.refName
}

object Target {

  trait Result

  type TargetInputMeta = Map[String, String]
  type BuildCmd[T] = Target[T] => Unit
  type ResultFn[T] = Target[T] => T
  type InputMetaFn[T] = Target[T] => TargetInputMeta
  type RunCmd[T] = (Target[T], List[String]) => Int

  class UnitResult extends Result
  object UnitResult extends UnitResult()

  object UnitResultFn {
    def apply(): ResultFn[UnitResult] = { t => UnitResult }
  }
  object EmptyBuildCmd {
    def apply[T](): BuildCmd[T] = { t => }
  }
  object EmptyInputMetaFn {
    def apply[T](): InputMetaFn[T] = { t => Map() }
  }
}
