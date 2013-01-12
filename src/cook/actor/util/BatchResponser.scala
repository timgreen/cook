package cook.actor.util

import scala.collection.mutable

/**
 * Batch message responser.
 */
class BatchResponser[Key, Value] {

  private val waiters = mutable.Map[Key, mutable.ListBuffer[Value]]()

  def onTask(key: Key, value: Value)(firstTimeAction: => Unit) {
    val list = waiters.getOrElseUpdate(key, mutable.ListBuffer[Value]())
    list += value
    if (list.size == 1) {
      firstTimeAction
    }
  }

  def complete(key: Key)(completeAction: Value => Unit) {
    waiters.remove(key) match {
      case None =>
      case Some(list) => list foreach completeAction
    }
  }
}
