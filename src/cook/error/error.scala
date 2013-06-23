package cook

import cook.console.ops._

import scala.util.{ Try, Success, Failure }

package object error {

  case class CookException(ops: ConsoleOps, e: Throwable) extends RuntimeException

  def error(consoleOps: => ConsoleOps) = new CookException(consoleOps, null)
  def error(e: Throwable)(consoleOps: => ConsoleOps) = new CookException(consoleOps, e)

  def reportError(consoleOps: => ConsoleOps): Nothing = throw error(consoleOps)
  def reportErrorIf(cond: Boolean)(consoleOps: => ConsoleOps) = if (cond) reportError(consoleOps)

  def wrapperError[T](consoleOps: => ConsoleOps)(op: => T): T = {
    try {
      op
    } catch {
      case e: Throwable =>
        throw new CookException(consoleOps, e)
    }
  }

  implicit def option2richOption[T](option: Option[T]) = new {
    def getOrError(consoleOps: => ConsoleOps): T = option getOrElse reportError(consoleOps)
  }
}
