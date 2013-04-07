package cook.actor.util

import scala.collection.mutable
import scala.concurrent.{ Promise, Future }
import scala.util.{ Try, Success, Failure }

/**
 * Batch message responser.
 */
class BatchResponser[Key, Result] {

  private val waiters = mutable.Map[Key, Promise[Result]]()

  def onTask(key: Key)(firstTimeAction: => Unit): Future[Result] = {
    waiters.getOrElseUpdate(key, {
      val p = Promise[Result]()
      firstTimeAction
      p
    }).future
  }

  def success(key: Key, result: Result) {
    waiters.remove(key) match {
      case None =>
      case Some(p) => p success result
    }
  }

  def failure(key: Key, e: Throwable) {
    waiters.remove(key) match {
      case None =>
      case Some(p) => p failure e
    }
  }

  def complete(key: Key)(tryResult: Try[Result]) {
    tryResult match {
      case Success(result) => success(key, result)
      case Failure(e) => failure(key, e)
    }
  }
}
