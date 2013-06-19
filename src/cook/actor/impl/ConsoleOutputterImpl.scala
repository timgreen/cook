package cook.actor.impl

import cook.actor.ConsoleOutputter
import cook.console.Console
import cook.error.CookException

import akka.actor.TypedActor
import scala.annotation.tailrec

class ConsoleOutputterImpl extends ConsoleOutputter {

  private var allowStatusUpdate = true

  override def printError(e: CookException) {
    @tailrec
    def doPrintError(e: CookException) {
      println(e.toString)
      if (e.e ne null) {
        doPrintError(e.e)
      }
    }

    doPrintError(e)
  }

  override def printUnknownError(e: Throwable) {
    // TODO(timgreen):
    e.printStackTrace
  }

  override def update(taskInfo: Set[(String, String)]) {
    Console.updateTaskInfo(taskInfo)
  }

  override def stopStatusUpdate {
    allowStatusUpdate = false
  }

  override def blockToFinish: Int = 0
}
