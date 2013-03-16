package cook.actor.util

import scala.collection.mutable
import scala.concurrent.{ Promise, Future }

/**
 * Batch message responser.
 */
class BatchResponser[Key, Result] {

  private val waiters = mutable.Map[Key, Promise[Result]]()

  def onTask(key: Key)(firstTimeAction: Promise[Result] => Unit): Future[Result] = {
    waiters.getOrElseUpdate(key, {
      val p = Promise[Result]()
      firstTimeAction(p)
      p
    }).future
  }

  def complete(key: Key, result: Result) {
    waiters.remove(key) match {
      case None =>
      case Some(p) => p success result
    }
  }
}
