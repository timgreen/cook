package cook.actors

import scala.collection.mutable.HashSet
import scala.collection.mutable.Queue

import cook.target._
import cook.util._

object Builder {

  def build(targetLabels: Seq[TargetLabel]) {
    val buildQueue = new Queue[Target]
    val labelsProccessed = new HashSet[String]

    for (l <- targetLabels) {
      buildQueue ++= Analysis.analyze(l)
    }
    for (
      t <- buildQueue
      if (!labelsProccessed.contains(t.targetName))
    ) {
      labelsProccessed += t.targetName
      t.build
    }
  }
}
