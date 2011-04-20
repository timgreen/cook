package cook.target

import scala.collection.mutable.HashMap

import cook.config.runner.ConfigType
import cook.config.runner.CookRunner
import cook.util.TargetLabel

object TargetManager {

  val targets = new HashMap[String, Target]

  def push(t: Target) {
    targets.put(t.name, t)
  }

  def getTarget(name: String): Target = {
    if (!hasTarget(name)) {
      val configFile = new TargetLabel(null, name).config
      CookRunner.run(configFile, ConfigType.COOK)
    }

    targets.get(name) match {
      case Some(target) => target
      case None => throw new TargetException("Target \"%s\" is not defined".format(name))
    }
  }

  private[target]
  def hasTarget(name: String) = targets.contains(name)

}
