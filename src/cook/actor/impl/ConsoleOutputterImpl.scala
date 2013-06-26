package cook.actor.impl

import cook.actor.ConsoleOutputter
import cook.actor.TargetStatus
import cook.app.Config
import cook.console.Console
import cook.console.ops._
import cook.error.CookException

import akka.actor.TypedActor
import scala.annotation.tailrec

class ConsoleOutputterImpl extends ConsoleOutputter {

  private var allowStatusUpdate = true

  override def printError(e: CookException) {
    @tailrec
    def doPrintError(e: Throwable) {
      e match {
        case CookException(ops, e) =>
          Console.print(ops :: newLine)
          if (e ne null) {
            doPrintError(e)
          }
        case _ =>
          import java.io.{ StringWriter, PrintWriter }
          val buffer = new StringWriter
          e.printStackTrace(new PrintWriter(buffer))
          Console.print(buffer.toString :: newLine)
      }
    }

    doPrintError(e)
  }

  override def printUnknownError(e: Throwable) {
    // TODO(timgreen):
    e.printStackTrace
  }

  override def update(targetStatus: TargetStatus, taskInfo: Set[(String, String)]) {
    if (allowStatusUpdate && !Config.quiet) {
      Console.update(
        done     = targetStatus.done,
        cached   = targetStatus.cached,
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
