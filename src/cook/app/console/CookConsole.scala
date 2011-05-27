package cook.app.console

import scala.collection.mutable.HashMap

object CookConsole {

  def println(s: String, objs: Any*) = {
    s.format(objs: _*).foreach(p)
    p('\n')
  }
  def print(s: String, objs: Any*) = s.format(objs: _*).foreach(p)

  def mark(name: String) {
    marks(name) = (x, y)
  }

  def clearToMark(name: String) {
    val (cx, cy) = (x, y)
    goto(x, y)
    val i = (x - cx) * width + (y - cy)
    reset
    for (x <- 1 to i) {
      p(' ')
    }
    goto(x, y)
  }

  def reset {
    control(Console.RESET)
  }

  private
  var x = 0
  var y = 0

  val marks = new HashMap[String, Tuple2[Int, Int]]

  def isLineEnd = (y + 1 == width)
  def isLineStart = (y == 0)

  def p(c: Char) {
    if (isLineEnd) {
      if (c != '\n') {
        Console.println
      }
      y += 1
      x = 0
    } else {
      x += 1
    }
    Console.print(c)
  }

  def width = {
    // TODO(timgreen): call native api to get columns
    80
  }

  def goto(x: Int, y: Int) {
    if (this.x > x) {
      moveLeft(this.x - x)
    } else if (this.x < x) {
      moveRight(x - this.x)
    }

    if (this.y > y) {
      moveUp(this.y - y)
    } else if (this.y < y) {
      moveDown(y - this.y)
    }
  }

  def moveUp(y: Int) {
    control("\033[%dA", y)
    this.y -= y
  }

  def moveDown(y: Int) {
    control("\033[%dB", y)
    this.y += y
  }

  def moveRight(x: Int) {
    control("\033[%dC", x)
    this.x += x
  }

  def moveLeft(x: Int) {
    control("\033[%dD", x)
    this.x -= x
  }

  def control(str: String, objs: Any*) {
    Console.printf(str, objs: _*)
  }
}

object CookConsoleHelper {

  def black(s: Any) = Console.BLACK + s.toString + Console.RESET
  def blue(s: Any) = Console.BLUE + s.toString + Console.RESET
  def cyan(s: Any) = Console.CYAN + s.toString + Console.RESET
  def yellow(s: Any) = Console.YELLOW + s.toString + Console.RESET
}
