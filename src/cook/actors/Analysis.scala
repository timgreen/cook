package cook.actors

import scala.collection.mutable.HashSet
import scala.collection.mutable.Stack
import scala.collection.immutable.VectorBuilder

import cook.target._
import cook.util._

object Analysis {

  def analyze(targetLabel: TargetLabel): Seq[Target] = {

    val targets = new VectorBuilder[Target]
    val labelsProccessing = new Stack[String]
    val labelsProccessingSet = new HashSet[String]
    val labelsProccessed = new HashSet[String]

    analyzeDeps(targetLabel, targets, labelsProccessing, labelsProccessingSet, labelsProccessed)

    targets.result.reverse
  }

  def analyzeDeps(
      targetLabel: TargetLabel,
      targets: VectorBuilder[Target],
      labelsProccessing: Stack[String],
      labelsProccessingSet: HashSet[String],
      labelsProccessed: HashSet[String]) {

    val target = TargetManager.getTarget(targetLabel)
    targets += target
    labelsProccessing.push(target.fullname)
    labelsProccessingSet += target.fullname
    for (dep <- target.depTargets) {
      if (labelsProccessingSet.contains(dep.targetName)) {
        // TODO(timgreen): better error message
        labelsProccessing.push(dep.targetName)
        throw new CookBaseException(
            "Found dependence circle: %s", labelsProccessing.mkString(" -> "))
      }
      if (!labelsProccessed.contains(dep.targetName)) {
        analyzeDeps(dep, targets, labelsProccessing, labelsProccessingSet, labelsProccessed)
      }
    }

    labelsProccessingSet -= labelsProccessing.pop
    labelsProccessed += target.fullname
  }
}
