package cook.target

import scala.tools.nsc.io.Path


class Target[T](
  val ref: TargetRef,
  val buildCmd: Target.BuildCmd[T],
  val resultFn: Target.ResultFn[T],
  val inputMetaFn: Target.InputMetaFn[T],
  val runCmd: Option[Target.RunCmd[T]],
  val deps: List[TargetRef]
) {

  private var _result: Option[T] = None
  private var built: Boolean = false
  private lazy val userInputMeta = inputMetaFn(this)
  private lazy val isMetaNotChanged = checkIfMetaChanged

  def result: T = {
    // TODO(timgreen):
    // only avaliable in build stage
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

  def build {
    if (needBuild) {
      buildCmd(this)
      built = true
    }
  }

  def buildDir = ref.buildDir
  def runDir = ref.runDir

  private def checkIfMetaChanged: Boolean = {
    // TODO(timgreen):
    true
  }
}

object Target {

  trait Result
  class UnitResult extends Result
  object UnitResult extends UnitResult()
  def unitResultFn[T] = { t: Target[T] => UnitResult }

  type TargetInputMeta = Map[String, String]
  type BuildCmd[T] = Target[T] => Unit
  type ResultFn[T] = Target[T] => T
  type InputMetaFn[T] = Target[T] => TargetInputMeta
  type RunCmd[T] = Target[T] => Int
}
