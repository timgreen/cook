package cook.actors

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.Stack
import scala.collection.immutable.VectorBuilder

import cook.target._
import cook.util._

object Analyst {

  def analyze(targetLabel: TargetLabel): Seq[Target] = {

    val labelsProccessing = new Stack[String]
    val labelsProccessingSet = new HashSet[String]
    val labelsProccessed = new HashSet[String]

    val outDeg = new HashMap[String, Int]
    val inEdges = new HashMap[String, VectorBuilder[String]]

    analyzeDeps(
        targetLabel,
        labelsProccessing,
        labelsProccessingSet,
        labelsProccessed,
        outDeg,
        inEdges)

    val readyToBuild = new Stack[String]
    val buildOrder = new VectorBuilder[Target]
    for ((t, i) <- outDeg) {
      if (i == 0) {
        readyToBuild.push(t)
      }
    }

    while (readyToBuild.nonEmpty) {
      val t = readyToBuild.pop
      buildOrder += TargetManager.getTarget(new TargetLabel("", t))

      for (tt <- inEdges.getOrElse(t, new VectorBuilder[String]).result) {
        val deg = outDeg(tt) - 1
        if (deg == 0) {
          readyToBuild.push(tt)
        } else {
          outDeg(tt) = deg
        }
      }
    }

    buildOrder.result
  }

  def analyzeDeps(
      targetLabel: TargetLabel,
      labelsProccessing: Stack[String],
      labelsProccessingSet: HashSet[String],
      labelsProccessed: HashSet[String],
      outDeg: HashMap[String, Int],
      inEdges: HashMap[String, VectorBuilder[String]]) {

    val target = TargetManager.getTarget(targetLabel)
    labelsProccessing.push(target.targetName)
    labelsProccessingSet += target.targetName

    val depTargets = target.deps
    outDeg.put(target.targetName, depTargets.length)

    for (dep <- depTargets) {
      inEdges.getOrElseUpdate(dep.targetName, new VectorBuilder[String]) += target.targetName

      if (labelsProccessingSet.contains(dep.targetName)) {
        // TODO(timgreen): better error message
        labelsProccessing.push(dep.targetName)
        throw new CookBaseException(
            "Found dependence circle: %s", labelsProccessing.mkString(" -> "))
      }
      if (!labelsProccessed.contains(dep.targetName)) {
        analyzeDeps(dep, labelsProccessing, labelsProccessingSet, labelsProccessed, outDeg, inEdges)
      }
    }

    labelsProccessingSet -= labelsProccessing.pop
    labelsProccessed += target.targetName
  }
}
