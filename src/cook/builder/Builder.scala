package cook.builder

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.Stack

import cook.config.runner.ConfigType
import cook.config.runner.CookRunner
import cook.util._

object Builder {

  def build(targetLabels: Seq[TargetLabel]) {
    buildStack.pushAll(targetLabels.reverse)
    while (buildStack.nonEmpty) {
      val targetLabel = buildStack.pop
      labelsProccessing.push(targetLabel)
      labelsProccessingSet += targetLabel.targetFullname
      buildOneTarget(targetLabel)
    }
  }

  def buildOneTarget(targetLabel: TargetLabel) {
    CookRunner.run(targetLabel.config, ConfigType.COOK)
    // TODO(timgreen)
    println("run target: %s".format(targetLabel.targetFullname))
  }

  val buildStack = new Stack[TargetLabel]
  val labelsProccessing = new Stack[TargetLabel]
  val labelsProccessingSet = new HashSet[String]
  val labelsProccessed = new HashSet[String]
}

class CookRuntimeExcetion(message: String) extends RuntimeException
