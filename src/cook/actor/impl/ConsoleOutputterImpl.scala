package cook.actor.impl

import cook.actor.ConsoleOutputter
import cook.actor.TargetStatus
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

  override def update(targetStatus: TargetStatus, taskInfo: Set[(String, String)]) {
    if (allowStatusUpdate) {
      Console.update(
        done     = targetStatus.done,
        cached   = 0,
        building = targetStatus.building,
        pending  = targetStatus.pending,
        unsolved = targetStatus.unsolved,
        taskInfo
      )
    }
  }

  override def stopStatusUpdate {
    allowStatusUpdate = false
  }

  override def blockToFinish: Int = 0
}
