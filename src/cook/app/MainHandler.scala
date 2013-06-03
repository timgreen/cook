package cook.app

import cook.error.CookException
import cook.actor.Actors.consoleOutputter

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Try, Success, Failure }

/**
 * Main handler.
 *
 * Block and wait other processes to finish, and handle exceptions.
 */
object MainHandler {

  def exec(processes: Future[_]*) {

    for (process <- processes) {
      try {
        Await.result(process, Duration.Inf)
      } catch {
        case e: Throwable =>
          handleException(e)
          return
      }
    }

    handleNormalExit
  }

  private def handleNormalExit {
    // TODO(timgreen):
  }

  def handleException(e: Throwable) = {
    consoleOutputter.stopStatusUpdate
    tryToStopWorkers
    e match {
      case e: CookException =>
        consoleOutputter.printError(e)
      case _: Throwable =>
        consoleOutputter.printUnknownError(e)
    }
    consoleOutputter.blockToFinish

    sys.exit(1)
  }

  private def tryToStopWorkers {
    // TODO(timgreen):
  }
}
