package cook.app.console

import cook.app.Config

trait ConsoleBase {

  def flush = Console.flush
  // According to http://en.wikipedia.org/wiki/ANSI_escape_code
  def newLine = Console.printf("\033E")
  def saveCursor = Console.printf("\033[s")
  def restoreCursor = Console.printf("\033[u")
  //def saveCursor = Console.printf("\0337")
  //def restoreCursor = Console.printf("\0338")
  def eraseToEnd = Console.printf("\033[J")
  def hideCursor = Console.printf("\033[?25l")
  def showCursor = Console.printf("\033[?25h")
  def reset = Console.printf(Console.RESET)
}


object CookConsole extends ConsoleBase {
  import Console._

  def printRootDir(rootDirStr: String) {
    reset
    printf("COOK_ROOT: %s%s%s", YELLOW, rootDirStr, RESET)
    newLine
    saveCursor
    flush
  }

  def updateProgress(done: Int, cached: Int, building: Int, pending: Int, unsolved: Int) {
    restoreCursor
    flush
    saveCursor
    hideCursor
    eraseToEnd

    // show info
    val total = done + cached + building + pending + unsolved
    printf("Find %s%d%s targets(s)", CYAN, total, RESET)
    newLine
    printf("Done %s%d%s Cached %s%d%s Building %s%d%s Pending %s%d%s Unsolved %s%d%s",
      CYAN, done, RESET,
      CYAN, cached, RESET,
      CYAN, building, RESET,
      CYAN, pending, RESET,
      CYAN, unsolved, RESET)
    newLine

    // draw progress bar
    val barWidth = width - 4 - 4
    val doneWidth = barWidth * done / total
    val cachedWidth = barWidth * cached / total
    val buildingWidth = barWidth * building / total
    val pendingWidth = barWidth * pending / total
    val unsolvedWidth = barWidth - doneWidth - cachedWidth - buildingWidth - pendingWidth
    printf("[ %s%s%s%s%s%s%s%s%s%s ]",
      CYAN,   "C" + "o" * doneWidth,
      YELLOW, "o" * (cachedWidth + 1),
      GREEN,  "o" * (buildingWidth + 1),
      BLUE,   "o" * pendingWidth + "k",
      RESET,  " " * unsolvedWidth)
    newLine
    showCursor
  }

  def updateBuildingTargets(targets: List[String]) {
    hideCursor
    for (t <- targets.headOption) {
      printf("%s%s%s", GREEN, t, RESET)
    }
    for (t <- targets.tail) {
      printf(", %s%s%s", GREEN, t, RESET)
    }
    newLine
    showCursor
  }

  def width = Config.cols
}
