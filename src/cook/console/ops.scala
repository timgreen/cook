package cook.console

import scala.util.DynamicVariable
import scala.{ Console => SConsole }

/** Console operation monad.
  *
  */
package object ops {

  case class ConsoleOption(
    isStyleEnable: Boolean,
    isControlEnable: Boolean,
    width: Int,
    writer: String => Unit
  )
  /** count from 0 */
  case class ConsoleStatus(
    cols: Int = 0,
    lines: Int = 0
  ) {
    def lineUsed = lines + 1
  }

  object ConsoleOps {

    val option = new DynamicVariable[ConsoleOption](null)
  }

  sealed trait ConsoleOps {
    private [console] def print(prevStatus: ConsoleStatus): ConsoleStatus
    def ::(o: ConsoleOps) = new CombineOps(o, this)
    def apply(ops: ConsoleOps): ConsoleOps = {
      this :: ops :: reset
    }
    def len: Int

    protected def option = ConsoleOps.option.value

    protected def calcStatus(prevStatus: ConsoleStatus): ConsoleStatus = {
      if (len == 0) {
        prevStatus
      } else {
        val chars = prevStatus.cols + len
        val linesAdd = (chars - 1) / option.width
        val newCols = (chars - 1) % option.width + 1
        ConsoleStatus(
          lines = prevStatus.lines,
          cols = newCols
        )
      }
    }
  }

  class CombineOps(a: ConsoleOps, b: ConsoleOps) extends ConsoleOps {
    private [console] override def print(prevStatus: ConsoleStatus): ConsoleStatus = {
      val s = a.print(prevStatus)
      b.print(s)
    }

    override def len: Int = a.len + b.len
  }

  private [console] class StringOps(s: String) extends ConsoleOps {
    private [console] override def print(prevStatus: ConsoleStatus): ConsoleStatus = {
      option.writer(s)
      calcStatus(prevStatus)
    }

    override def len: Int = s.length
  }

  class StyleOps(style: String) extends StringOps(style) {
    private [console] override def print(prevStatus: ConsoleStatus): ConsoleStatus = {
      if (option.isStyleEnable) {
        option.writer(style)
      }
      prevStatus
    }

    override def len: Int = 0
  }
  val reset = new StyleOps(SConsole.RESET)

  class ControlOps(control: String) extends StringOps(control) {
    private [console] override def print(prevStatus: ConsoleStatus): ConsoleStatus = {
      if (option.isControlEnable) {
        option.writer(control)
      }
      prevStatus
    }

    override def len: Int = 0
  }

  val black      = new StyleOps(SConsole.BLACK)
  val blink      = new StyleOps(SConsole.BLINK)
  val blue       = new StyleOps(SConsole.BLUE)
  val bold       = new StyleOps(SConsole.BOLD)
  val cyan       = new StyleOps(SConsole.CYAN)
  val green      = new StyleOps(SConsole.GREEN)
  val magenta    = new StyleOps(SConsole.MAGENTA)
  val red        = new StyleOps(SConsole.RED)
  val underlined = new StyleOps(SConsole.UNDERLINED)
  val white      = new StyleOps(SConsole.WHITE)
  val yellow     = new StyleOps(SConsole.YELLOW)


  object flush extends ConsoleOps {
    private [console] override def print(prevStatus: ConsoleStatus): ConsoleStatus = {
      // TODO(timgreen):
      // SConsole.flush
      prevStatus
    }

    override def len: Int = 0
  }

  // According to http://en.wikipedia.org/wiki/ANSI_escape_code
  object newLine extends StringOps("\n") {
    private [console] override def print(prevStatus: ConsoleStatus): ConsoleStatus = {
      option.writer("\n")
      ConsoleStatus(
        cols = 0,
        lines = prevStatus.lines + 1
      )
    }

    override def len: Int = 0
  }
  val eraseToEnd = new ControlOps("\033[J")
  val hideCursor = new ControlOps("\033[?25l")
  val showCursor = new ControlOps("\033[?25h")
  case class prevLine(lines: Int) extends ControlOps(if (lines > 0) "\033[" + lines + "F" else "")

  // defines
  val strong = yellow :: bold
  val indent = new StringOps("  ")

  // implicits
  implicit def string2ops(s: String): ConsoleOps = {
    s.split('\n') map { new StringOps(_).asInstanceOf[ConsoleOps] } reduce { _ :: newLine :: _ }
  }

}
