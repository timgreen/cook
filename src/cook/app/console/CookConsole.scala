package cook.app.console

object CookConsole {

  def println(s: String) = Console.println(s)
  def print(s: String) = Console.print(s)

  private

  def width = {
    // TODO(timgreen): call native api to get columns
    val col = 80
    if (col == null) {
      80
    } else {
      col.toInt
    }
  }

}
