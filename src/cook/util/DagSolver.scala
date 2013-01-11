package cook.util

import scala.annotation.tailrec
import scala.collection.mutable


/**
 * Help to find Topological sorting in dag.
 */
class DagSolver {

  private val doneNodes = mutable.Set[String]()
  private val processingNodes = mutable.Set[String]()
  private val avaliableNodes = mutable.Queue[String]()
  private val depNodesCount = mutable.Map[String, Int]()
  private val nodesDepOnMe = mutable.Map[String, mutable.ListBuffer[String]]()

  import DagSolver._

  def addDeps(node: String, deps: List[String]): AddDepsResult =  {
    if (deps.isEmpty) {
      avaliableNodes += node
      Ok
    } else {
      val set = deps.toSet
      depNodesCount(node) = set.size
      set foreach { dep =>
        nodesDepOnMe.getOrElseUpdate(dep, mutable.ListBuffer()) += node
      }
      checkCycle(node, List(node))
    }
  }


  @tailrec
  private def checkCycle(startNode: String, pendingNodes: List[String],
    checkedNodes: mutable.Set[String] = mutable.Set(),
    fromPath: mutable.Map[String, String] = mutable.Map()): AddDepsResult = {
    pendingNodes match {
      case Nil =>
        // done & no cycle found
        Ok
      case node :: tail =>
        if (node == startNode) {
          FoundDepCycle(buildCycleSeq(startNode, startNode, fromPath))
        } else {
          val extendPending = nodesDepOnMe.getOrElse(node, List()) filterNot checkedNodes.contains
          checkedNodes += node
          extendPending foreach { d =>
            fromPath(d) = node
          }
          checkCycle(startNode, tail ::: extendPending.toList, checkedNodes, fromPath)
        }
    }
  }

  @tailrec
  private def buildCycleSeq(startNode: String, currentNode: String,
    fromPath: mutable.Map[String, String],
    seq: List[String] = Nil): List[String] = {
    if (currentNode == startNode) {
      seq
    } else {
      buildCycleSeq(startNode, fromPath(currentNode), fromPath, currentNode :: seq)
    }
  }

  def hasAvaliable = avaliableNodes.nonEmpty

  def pop: String = {
    val node = avaliableNodes.dequeue
    processingNodes += node
    node
  }

  def markDone(doneNode: String) {
    doneNodes += doneNode
    processingNodes -= doneNode
    for {
      edges <- nodesDepOnMe.get(doneNode)
      node <- edges
    } {
      depNodesCount(node) -= 1
      if (depNodesCount(node) == 0) {
        avaliableNodes enqueue node
      }
    }
  }
}

object DagSolver {

  sealed trait AddDepsResult
  case object Ok extends AddDepsResult
  case class FoundDepCycle(cycle: List[String]) extends AddDepsResult
}
