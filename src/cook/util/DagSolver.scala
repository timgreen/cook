package cook.util

import scala.annotation.tailrec
import scala.collection.mutable


case class DagStatus(done: Int, processing: Int, pending: Int, avaliable: Int, unsolved: Int)

/**
 * Help to find topological sorting on dag.
 *
 * Use addDeps to add all deps for one node at once.
 */
class DagSolver {

  private val nodes = mutable.Set[String]()
  private val doneNodes = mutable.Set[String]()
  private val processingNodes = mutable.Set[String]()
  private val avaliableNodes = mutable.Queue[String]()
  private val depNodesCount = mutable.Map[String, Int]()
  private val nodesDepOnMe = mutable.Map[String, mutable.ListBuffer[String]]()
  /**
   * Nodes already known deps.
   */
  private val solvedNodes = mutable.Set[String]()

  def getStatus: DagStatus = {
    val done = doneNodes.size
    val processing = processingNodes.size
    val avaliable = avaliableNodes.size
    val pending = solvedNodes.size - done - processing - avaliable
    val unsolved = nodes.size - solvedNodes.size

    assert(done >= 0, "done nodes must >= 0")
    assert(processing >= 0, "processing nodes must >= 0")
    assert(avaliable >= 0, "avaliable nodes must >= 0")
    assert(pending >= 0, "pending nodes must >= 0")
    assert(unsolved >= 0, "unsolved nodes must >= 0")

    DagStatus(
      done = done,
      processing = processing,
      pending = pending,
      avaliable = avaliable,
      unsolved = unsolved
    )
  }

  import DagSolver._

  def addDeps(node: String, deps: Seq[String]): AddDepsResult =  {
    assert(!solvedNodes.contains(node), "Duplicated node: " + node)
    nodes += node
    solvedNodes += node

    val set = deps.toSet -- doneNodes
    if (set.isEmpty) {
      avaliableNodes += node
      Ok
    } else {
      nodes ++= set
      depNodesCount(node) = set.size
      set foreach { dep =>
        nodesDepOnMe.getOrElseUpdate(dep, mutable.ListBuffer()) += node
      }
      checkCycle(node, nodesDepOnMe.getOrElse(node, Nil).toList)
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
          val extendPending = nodesDepOnMe.getOrElse(node, Nil) filterNot checkedNodes.contains
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
}
