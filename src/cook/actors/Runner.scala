package cook.actors

import scala.collection.mutable.HashSet
import scala.collection.mutable.Queue

import cook.target._
import cook.util._

object Runner {

  def run(targetLabel: TargetLabel) {
    println("Running target \"%s\"".format(targetLabel.targetName))
    val target = TargetManager.getTarget(targetLabel)
    target.execute
  }
}

