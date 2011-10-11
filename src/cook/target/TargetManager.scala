package cook.target

import scala.collection.mutable.HashMap

import cook.config.runner.ConfigType
import cook.config.runner.CookRunner
import cook.error.ErrorMessageHandler
import cook.util.TargetLabel

object TargetManager extends ErrorMessageHandler {

  val targets = new HashMap[String, Target]

  def push(t: Target) {
    targets.put(t.targetName, t)
  }

  def getTarget(targetFullName: String): Target = {
    val targetLabel = new TargetLabel("", targetFullName)
    getTarget(targetLabel)
  }

  def getTarget(targetLabel: TargetLabel): Target = {
    if (!hasTarget(targetLabel.targetName)) {
      CookRunner.run(targetLabel.config, ConfigType.COOK)
    }

    targets.get(targetLabel.targetName) match {
      case Some(target) => target
      case None => {
        reportError("Target \"%s\" is not defined".format(targetLabel.targetName))
      }
    }
  }

  private[target]
  def hasTarget(name: String) = targets.contains(name)

}
