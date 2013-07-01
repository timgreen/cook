package cook.app

import cook.actor.Actors
import cook.actor.Actors.consoleOutputter
import cook.app.version.VersionMeta
import cook.error._
import cook.meta.db.DbProvider.{ db => metaDb }

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
    Actors.targetBuilder.blockToFinish
    Actors.statusManager.blockToFinish
    Actors.consoleOutputter.blockToFinish
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
    metaDb.close
    if (shutdownHookThread ne null) {
      shutdownHookThread.remove
    }
  }

  private var shutdownHookThread: sys.ShutdownHookThread = _
  def prepareMetaDb {
    metaDb.open
    shutdownHookThread = sys.addShutdownHook {
      metaDb.close
    }
  }

  def cleanMetaDbIfCookVersionChanged {
    val m = VersionMeta()
    if (metaDb.get(VersionMeta.key) != m) {
      metaDb.clean
      metaDb.put(VersionMeta.key, m)
    }
  }
}
