package cook.app

object Stage extends Enumeration {
  type Stage = Value
  val Init, Analysis, Building, Running = Value

  private var _stage: Stage = Init
  def stage = _stage
  private[app] def stage_=(stage: Stage) {
    _stage = stage
  }

  def isTargetResultReady = (stage != Init) && (stage != Analysis)
}
