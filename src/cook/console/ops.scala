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
  object reset extends StyleOps(SConsole.RESET)

  trait StyleWrapper { this: StyleOps =>
    def apply(ops: ConsoleOps): ConsoleOps = {
      this :: ops :: reset
    }
  }

  class ControlOps(control: String) extends StringOps(control) {
    private [console] override def print {
      if (ConsoleOps.isControlEnable) {
        super.print
      }
    }
  }

  object black      extends StyleOps(SConsole.BLACK)      with StyleWrapper
  object blink      extends StyleOps(SConsole.BLINK)      with StyleWrapper
  object blue       extends StyleOps(SConsole.BLUE)       with StyleWrapper
  object bold       extends StyleOps(SConsole.BOLD)       with StyleWrapper
  object cyan       extends StyleOps(SConsole.CYAN)       with StyleWrapper
  object green      extends StyleOps(SConsole.GREEN)      with StyleWrapper
  object magenta    extends StyleOps(SConsole.MAGENTA)    with StyleWrapper
  object red        extends StyleOps(SConsole.RED)        with StyleWrapper
  object underlined extends StyleOps(SConsole.UNDERLINED) with StyleWrapper
  object white      extends StyleOps(SConsole.WHITE)      with StyleWrapper
  object yellow     extends StyleOps(SConsole.YELLOW)     with StyleWrapper


  implicit def string2ops(s: String): ConsoleOps = new StringOps(s)

  object flush extends ConsoleOps {
    private [console] override def print {
      SConsole.flush
    }
  }

  // According to http://en.wikipedia.org/wiki/ANSI_escape_code
  object newLine extends StringOps("\n")
  //object saveCursor extends ControlOps("\033[s\0337")
  //object restoreCursor extends ControlOps("\033[u\0338")
  object eraseToEnd extends ControlOps("\033[J")
  object hideCursor extends ControlOps("\033[?25l")
  object showCursor extends ControlOps("\033[?25h")
  case class prevLine(lines: Int) extends ControlOps(if (lines > 0) "\033[" + lines + "F" else "")
}
