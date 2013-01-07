package cook.actor.util

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable

class WorkerTokenManagerActor(val maxWorkerNum: Int) extends Actor {

  private val availableTokens = mutable.Stack[Int]()
  private val askers = mutable.Queue[ActorRef]()
  private val askerSet = mutable.Set[ActorRef]()

  def receive = {
    case AskWorkerToken =>
      if (!askerSet.contains(sender)) {
        askerSet += sender
        askers enqueue sender
      }
      check
    case ReturnWorkerToken(token) =>
      availableTokens push token
      check
  }

  def check {
    if (availableTokens.nonEmpty && askers.nonEmpty) {
      val token = availableTokens.pop
      val asker = askers.dequeue
      askerSet -= asker
      asker ! AssignWorkerToken(token)
    }
  }
}
