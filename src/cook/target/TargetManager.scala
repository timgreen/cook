package cook.target

object TargetManager {

  def getTarget(ref: TargetRef): Target[_] = {
    ConfigEngine.load(ref.containerConfigRef).getTarget(ref.name)
  }
}
