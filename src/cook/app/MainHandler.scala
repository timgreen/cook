package cook.app

import cook.actor.Actors.consoleOutputter
import cook.error.CookException
import cook.meta.db.DbProvider

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
    shutdownCleanUp
    Global.system.shutdown
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
    shutdownCleanUp
    consoleOutputter.blockToFinish

    sys.exit(1)
  }

  private def tryToStopWorkers {
    // TODO(timgreen):
  }

  private def shutdownCleanUp {
    DbProvider.db.close
  }
}
