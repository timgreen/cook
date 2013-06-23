package cook.console

import cook.app.Config
import cook.console.ops._

import scala.{ Console => SConsole }


/** Example console output for 80-cols:
  * <code>
  * COOK_ROOT: xxxxxxxxxx
  * Find x target(s): Done x Cached x Building x Pending x Unsolved x
  * [Cooooooooooooooooooooooooooooooooooooooooook                                 ]
  * Loading Config: xxxxxx
  * BuildingTarget: xxxxxx
  * </code>
  *
  */
object Console {

  def print(ops: ConsoleOps): ConsoleStatus = {
    val option = ConsoleOption(
      isStyleEnable = true,
      isControlEnable = true,
      width = Config.cols,
      writer = SConsole.print,
      flushFn = SConsole.flush
    )
    ConsoleOps.option.withValue(option) {
      (ops :: flush).print(ConsoleStatus())
    }
  }

  def cookRootNotFound = print {
    red("COOK_ROOT Not Found") :: newLine
  }

  def printRootDir(rootDirStr: String) = print {
    reset ::
    "COOK_ROOT: " :: yellow(rootDirStr) ::
    newLine
  }

  def CookRootFormatError(msg: String) = print {
    red("COOK_ROOT format error") ::
    newLine ::
    msg ::
    newLine
  }

  private var lineUsed = 0
  def update(done: Int, cached: Int, building: Int, pending: Int, unsolved: Int, taskInfo: Set[(String, String)]) {
    val s = print {
      val moveCursorOps =
        hideCursor    ::
        prevLine(lineUsed) ::
        eraseToEnd

      // show info
      val total = done + cached + building + pending + unsolved

      val statusInfoOps = if (unsolved > 0) {
        "Finding "  :: cyan(total.toString)    :: " target(s): " ::
        "Done "     :: cyan(done.toString)     :: " " ::
        "Cached "   :: cyan(cached.toString)   :: " " ::
        "Building " :: cyan(building.toString) :: " " ::
        "Pending "  :: cyan(pending.toString)  :: " " ::
        "Unsolved " :: cyan(unsolved.toString)        ::
        newLine
      } else {
        "Found "    :: cyan(total.toString)    :: " target(s): " ::
        "Done "     :: cyan(done.toString)     :: " " ::
        "Cached "   :: cyan(cached.toString)   :: " " ::
        "Building " :: cyan(building.toString) :: " " ::
        "Pending "  :: cyan(pending.toString)  :: " " ::
        newLine
      }

      // draw progress bar
      val barWidth = w - 4 - 4
      val doneWidth = barWidth * done / total
      val cachedWidth = barWidth * cached / total
      val buildingWidth = barWidth * building / total
      val pendingWidth = barWidth * pending / total
      val unsolvedWidth = barWidth - doneWidth - cachedWidth - buildingWidth - pendingWidth

      val statusBarOps = "[ "                ::
        cyan    :: "C" :: "o" * doneWidth    ::
        yellow  :: "o" * (cachedWidth   + 1) ::
        green   :: "o" * (buildingWidth + 1) ::
        blue    :: "o" * pendingWidth :: "k" ::
        reset   :: " " * unsolvedWidth       ::
        " ]"                                 ::
        newLine

      val tasksOps =
        taskInfo.toList.sorted.map { case (taskType, taskName) =>
          cyan(taskType) :: ": " :: taskName :: newLine
        }

      moveCursorOps :: statusInfoOps :: statusBarOps :: tasksOps ::: showCursor
    }

    lineUsed = s.lineUsed
  }

  private def w = Config.cols
}
