package cook.app

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

  private def handleException(e: Throwable) {
    // TODO(timgreen):
  }
}
