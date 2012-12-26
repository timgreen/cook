package cook.app

import cook.app.console.CookConsole

object Main {

  def main(args: Array[String]) {
    findAndPrintRootDir
  }

  def findAndPrintRootDir {

    CookConsole.printRootDir("asdfaf")
    CookConsole.updateProgress(1, 2, 3, 4, 5)
    CookConsole.updateBuildingTargets(List("a", "bbbbb", "ccccc", "ddddd", "eeee", "f" * 10))
    CookConsole.updateProgress(2, 3, 4, 5, 6)

  }
}
