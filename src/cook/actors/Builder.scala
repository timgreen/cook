package cook.actors

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.Stack

import cook.target._
import cook.util._

object Builder {

  def build(targetLabels: Seq[TargetLabel]) {
    buildStack.pushAll(targetLabels.reverse)
    while (buildStack.nonEmpty) {
      val targetLabel = buildStack.pop
      labelsProccessing.push(targetLabel)
      labelsProccessingSet += targetLabel.targetName
      buildOneTarget(targetLabel)
      labelsProccessing.pop
    }
  }

  def buildOneTarget(targetLabel: TargetLabel) {
    val target = TargetManager.getTarget(targetLabel)
    println("analysis target: %s".format(targetLabel.targetName))

    target.depTargets.foreach { buildStack.push(_) }
  }

  val buildStack = new Stack[TargetLabel]
  val labelsProccessing = new Stack[TargetLabel]
  val labelsProccessingSet = new HashSet[String]
  val labelsProccessed = new HashSet[String]
}

class CookRuntimeExcetion(message: String) extends RuntimeException
