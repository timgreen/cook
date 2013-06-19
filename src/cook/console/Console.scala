package cook.console

import cook.app.Config
import cook.console.ops._

import scala.{ Console => SConsole }


/** Example console output for 80-cols:
  * <code>
  * COOK_ROOT: xxxxxxxxxx
    Find x target(s)
  * Done x Cached x Building x Pending x Unsolved x
  * [Cooooooooooooooooooooooooooooooooooooooooook                                 ]
  * Loading Config: xxxxxx
  * BuildingTarget: xxxxxx
  * </code>
  *
  */
object Console {

  def print(ops: ConsoleOps) {
    ops.print
    flush.print
  }

  def cookRootNotFound = print {
    reset + red("COOK_ROOT Not Found") + newLine
  }

  def printRootDir(rootDirStr: String) = print {
    reset +
    "COOK_ROOT: " + yellow(rootDirStr) +
    newLine +
    saveCursor
  }

  def CookRootFormatError(msg: String) = print {
    reset +
    red("COOK_ROOT format error") +
    newLine +
    msg +
    newLine
  }

  def updateProgress(done: Int, cached: Int, building: Int, pending: Int, unsolved: Int) = print {
    val clearOps =
      restoreCursor +
      flush +
      saveCursor +
      hideCursor +
      eraseToEnd

    // show info
    val total = done + cached + building + pending + unsolved

    val targetInfoOps =
      "Find " + cyan(total.toString) + " target(s)" +
      newLine

    val statusInfoOps =
      "Done "     + cyan(done.toString)     + " " +
      "Cached "   + cyan(cached.toString)   + " " +
      "Building " + cyan(building.toString) + " " +
      "Pending "  + cyan(pending.toString)  + " " +
      "Unsolved " + cyan(unsolved.toString) +
      newLine

    // draw progress bar
    val barWidth = Config.cols - 4 - 4
    val doneWidth = barWidth * done / total
    val cachedWidth = barWidth * cached / total
    val buildingWidth = barWidth * building / total
    val pendingWidth = barWidth * pending / total
    val unsolvedWidth = barWidth - doneWidth - cachedWidth - buildingWidth - pendingWidth

    val statusBarOps = "[ "                +
      cyan    + "C" + "o" * doneWidth      +
      yellow  + "o" * (cachedWidth   + 1)  +
      green   + "o" * (buildingWidth + 1)  +
      blue    + "o" * pendingWidth   + "k" +
      reset   + " " * unsolvedWidth        +
      " ]"                                 +
      newLine + showCursor

    clearOps + targetInfoOps + statusInfoOps + statusBarOps
  }

  def updateTaskInfo(taskInfo: Set[(String, String)]) = print {
    val tasks =
      taskInfo.toList.sorted.map { case (taskType, taskName) =>
        cyan(taskType) + ": " + taskName
      }

    tasks.fold(hideCursor)( _ + _ ) +
    newLine + showCursor
  }
}
