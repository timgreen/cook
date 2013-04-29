package cook.util

import scala.annotation.tailrec
import scala.collection.mutable


/**
 * Help to find topological sorting on dag.
 *
 * Use addDeps to add all deps for one node at once.
 */
class DagSolver {

  private val doneNodes = mutable.Set[String]()
  private val processingNodes = mutable.Set[String]()
  private val avaliableNodes = mutable.Queue[String]()
  private val depNodesCount = mutable.Map[String, Int]()
  private val nodesDepOnMe = mutable.Map[String, mutable.ListBuffer[String]]()
  /**
   * Nodes already known deps.
   */
  private val solvedNodes = mutable.Set[String]()

  import DagSolver._

  def addDeps(node: String, deps: List[String]): AddDepsResult =  {
    if (solvedNodes.contains(node)) {
      FoundDuplicatedNode(node)
    } else {
      solvedNodes += node
      if (deps.isEmpty) {
        avaliableNodes += node
        Ok
      } else {
        val set = deps.toSet
        depNodesCount(node) = set.size
        set foreach { dep =>
          nodesDepOnMe.getOrElseUpdate(dep, mutable.ListBuffer()) += node
        }
        checkCycle(node, nodesDepOnMe.getOrElse(node, List()).toList)
      }
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
          FoundDepCycle(buildCycleSeq(startNode, fromPath))
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
  private def buildCycleSeq(currentNode: String, fromPath: mutable.Map[String, String],
    seq: List[String] = Nil): List[String] = {
    fromPath.get(currentNode) match {
      case None => currentNode :: seq
      case Some(prevNode) =>
        buildCycleSeq(prevNode, fromPath, currentNode :: seq)
    }
  }

  def hasAvaliable = avaliableNodes.nonEmpty
  def avaliableNodesSet: Set[String] = avaliableNodes.toSet

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
  case class FoundDuplicatedNode(node: String) extends AddDepsResult
}
