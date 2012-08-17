package cook.target


class Target[T <: Target.Result](
  val ref: TargetRef,
  buildCmd: Target.BuildCmd[T],
  resultFn: Target.ResultFn[T],
  inputMetaFn: Target.InputMetaFn[T],
  runCmd: Option[Target.RunCmd[T]],
  deps: List[TargetRef]
) {

  private var _result: Option[T] = None
  private var built: Boolean = false
  private lazy val inputMeta = inputMetaFn(this)
  private lazy val isMetaNotChanged = checkIfMetaChanged(inputMeta)

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

  def needBuild = !built && isMetaNotChanged

  def build {
    if (needBuild) {
      buildCmd(this)
      built = true
    }
  }

  private def checkIfMetaChanged(inputMeta: Target.TargetInputMeta): Boolean = {
    // TODO(timgreen):
    true
  }
}

object Target {

  trait Result
  class UnitResult extends Result
  object UnitResult extends UnitResult()

  type TargetInputMeta = Map[String, String]
  type BuildCmd[T <: Result] = Target[T] => Unit
  type ResultFn[T <: Result] = Target[T] => T
  type InputMetaFn[T <: Result] = Target[T] => TargetInputMeta
  type RunCmd[T <: Result] = Target[T] => Int
}
