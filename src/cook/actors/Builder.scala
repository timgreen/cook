package cook.actors

import scala.collection.mutable.HashSet
import scala.collection.mutable.Queue

import cook.target._
import cook.util._

object Builder {

  def build(targetLabels: Seq[TargetLabel]) {
    val buildQueue = new Queue[Target]
    val targets= new HashSet[String]

    for (tl <- targetLabels) {
      val analyst = Analyst(tl)
      while (analyst.nonEmpty) {
        val t = analyst.get.get
        analyst.setDone(t)
        if (!targets.contains(t)) {
          buildQueue += TargetManager.getTarget(t)
        }
      }
    }

    buildQueue.foreach(_.build)
  }
}
