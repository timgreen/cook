package cook.console

import scala.{ Console => SConsole }

package object ops {

  object ConsoleOps {
    def isStyleEnable: Boolean = true
    def isControlEnable: Boolean = true
  }

  sealed trait ConsoleOps {
    private [console] def print
    def ::(o: ConsoleOps) = new CombineOps(o, this)
    def apply(ops: ConsoleOps): ConsoleOps = {
      this :: ops :: reset
    }
  }

  class CombineOps(a: ConsoleOps, b: ConsoleOps) extends ConsoleOps {
    private [console] override def print {
      a.print
      b.print
    }
  }

  class StringOps(s: String) extends ConsoleOps {
    private [console] override def print {
      SConsole.print(s)
    }
  }

  class StyleOps(style: String) extends StringOps(style) {
    private [console] override def print {
      if (ConsoleOps.isStyleEnable) {
        super.print
      }
    }
  }
  val reset = new StyleOps(SConsole.RESET)

  class ControlOps(control: String) extends StringOps(control) {
    private [console] override def print {
      if (ConsoleOps.isControlEnable) {
        super.print
      }
    }
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


  implicit def string2ops(s: String): ConsoleOps = new StringOps(s)

  object flush extends ConsoleOps {
    private [console] override def print {
      SConsole.flush
    }
  }

  // According to http://en.wikipedia.org/wiki/ANSI_escape_code
  val newLine = new StringOps("\n")
  //object saveCursor extends ControlOps("\033[s\0337")
  //object restoreCursor extends ControlOps("\033[u\0338")
  val eraseToEnd = new ControlOps("\033[J")
  val hideCursor = new ControlOps("\033[?25l")
  val showCursor = new ControlOps("\033[?25h")
  case class prevLine(lines: Int) extends ControlOps(if (lines > 0) "\033[" + lines + "F" else "")

  // defines
  val strong = yellow :: bold
  val indent = new StringOps("  ")
}
