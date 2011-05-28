package cook.app.console

import scala.collection.mutable.HashMap

object CookConsole {

  def println(s: String, objs: Any*) {
    s.format(objs: _*).foreach(p)
    p('\n')
  }
  def print(s: String, objs: Any*) {
    s.format(objs: _*).foreach(p)
    Console.flush
  }

  def mark(name: Symbol) {
    marks(name) = (x, y)
  }

  def clearToMark(name: Symbol) {
    val (mx, my) = marks(name)
    val (cx, cy) = (x, y)
    goto(mx, my)
    val spaces = (cy - my) * width + (cx - mx)
    reset
    for (i <- 1 to spaces) {
      p(' ')
    }
    goto(mx, my)
  }

  def reset {
    control(Console.RESET)
  }

  def control(str: String, objs: Any*) {
    Console.printf(str, objs: _*)
  }

  def cookPercentage(percentage: Int) {
    val remain = width - x - 2 - 4 - 2
    val os = remain * percentage / 100
    val spaces = remain - os

    print("[ ")
    control(Console.CYAN)
    p('C')
    control(Console.YELLOW)
    p('o')

    control(Console.GREEN)
    p('o')
    for (i <- 1 to os) {
      p('o')
    }
    control(Console.CYAN)
    p('k')
    for (i <- 1 to spaces) {
      p(' ')
    }
    reset
    print(" ]")
  }

  private
  var x = 0
  var y = 0

  val marks = new HashMap[Symbol, Tuple2[Int, Int]]

  def isLineEnd = (x + 1 == width)
  def isLineStart = (x == 0)

  def p(c: Char) {
    Console.print(c)
    if (isLineEnd) {
      if (c != '\n') {
        Console.println
      }
      y += 1
      x = 0
    } else if (c == '\n') {
      y += 1
      x = 0
    } else {
      x += 1
    }
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

  def moveUp(dy: Int) {
    control("\033[%dA", dy)
    y -= dy
  }

  def moveDown(dy: Int) {
    control("\033[%dB", dy)
    y += dy
  }

  def moveRight(dx: Int) {
    control("\033[%dC", dx)
    x += dx
  }

  def moveLeft(dx: Int) {
    control("\033[%dD", dx)
    x -= dx
  }

}
