package cook.actors

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.Stack
import scala.collection.immutable.VectorBuilder

import cook.app.console.CookConsole
import cook.target._
import cook.util._

class Analyst {

  def isEmpty = (remain == 0)
  def nonEmpty = (remain != 0)

  def available = readyToBuild.size
  def get: Option[String] = {
    if (readyToBuild.nonEmpty) {
      remain = remain - 1
      Some(readyToBuild.pop)
    } else {
      None
    }
  }

  def setBuilding(target: String) {
    building += target
  }

  def setCached(target: String) {
    building -= target
    cached += target
    markFinished(target)
  }

  def setBuilt(target: String) {
    building -= target
    built += target
    markFinished(target)
  }

  def total = n
  val built = new HashSet[String]
  val building = new HashSet[String]
  val cached = new HashSet[String]

  private

  def markFinished(target: String) {
    outEdges.get(target) match {
      case Some(vb) =>
        for (t <- vb.result) {
          val deg = inDeg(t) - 1
          if (deg == 0) {
            readyToBuild.push(t)
          } else {
            inDeg(t) = deg
          }
        }
      case None =>
    }
  }

  def addDeps(a: String, b: String) {
    nodes += a
    nodes += b
    inDeg(b) = inDeg.getOrElse(b, 0) + 1
    outEdges.getOrElseUpdate(a, new VectorBuilder[String]) += b
  }

  def init = {
    for (v <- nodes if !inDeg.contains(v)) {
      readyToBuild.push(v)
    }

    n = nodes.size
    remain = n

    CookConsole.print("Find ")
    CookConsole.control(Console.CYAN)
    CookConsole.print("%d", n)
    CookConsole.reset
    CookConsole.println(" target(s)")

    this
  }

  var n = 0
  var remain = 0
  val nodes = new HashSet[String]
  val inDeg = new HashMap[String, Int]
  val outEdges = new HashMap[String, VectorBuilder[String]]
  val readyToBuild = new Stack[String]
}

object Analyst {

  def apply(targetLabels: TargetLabel*): Analyst = {

    val analyst = new Analyst

    val targetsProccessing = new Stack[String]
    val targetsProccessingSet = new HashSet[String]
    val targetsProccessed = new HashSet[String]

    targetLabels.foreach {
      analyzeDeps(
          _,
          targetsProccessing,
          targetsProccessingSet,
          targetsProccessed,
          analyst)
    }

    analyst.init
  }

  private
  def analyzeDeps(
      targetLabel: TargetLabel,
      targetsProccessing: Stack[String],
      targetsProccessingSet: HashSet[String],
      targetsProccessed: HashSet[String],
      analyst: Analyst) {

    val target = TargetManager.getTarget(targetLabel)
    targetsProccessing.push(target.targetName)
    targetsProccessingSet += target.targetName

    for (dep <- target.deps) {
      analyst.addDeps(dep.targetName, target.targetName)

      if (targetsProccessingSet.contains(dep.targetName)) {
        // TODO(timgreen): better error message
        targetsProccessing.push(dep.targetName)
        throw new CookBaseException(
            "Found dependence circle: %s", targetsProccessing.mkString(" -> "))
      }
      if (!targetsProccessed.contains(dep.targetName)) {
        analyzeDeps(dep, targetsProccessing, targetsProccessingSet, targetsProccessed, analyst)
      }
    }

    targetsProccessingSet -= targetsProccessing.pop
    targetsProccessed += target.targetName
  }
}
