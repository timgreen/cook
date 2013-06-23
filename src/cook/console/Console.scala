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

  def print(ops: ConsoleOps) {
    val option = ConsoleOption(
      isStyleEnable = true,
      isControlEnable = true,
      width = Config.cols,
      writer = SConsole.print
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
  def update(done: Int, cached: Int, building: Int, pending: Int, unsolved: Int, taskInfo: Set[(String, String)]) = print {
    val moveCursorOps =
      hideCursor    ::
      prevLine(lineUsed) ::
      eraseToEnd

    lineUsed = 2

    // show info
    val total = done + cached + building + pending + unsolved

    val statusInfoOps =
      "Find "     :: cyan(total.toString)    :: " target(s): " ::
      "Done "     :: cyan(done.toString)     :: " " ::
      "Cached "   :: cyan(cached.toString)   :: " " ::
      "Building " :: cyan(building.toString) :: " " ::
      "Pending "  :: cyan(pending.toString)  :: " " ::
      "Unsolved " :: cyan(unsolved.toString)        ::
      newLine

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

    val tasks =
      taskInfo.toList.sorted.map { case (taskType, taskName) =>
        lineUsed += (taskType.size + taskType.size + 2 + w - 1) / w
        cyan(taskType) :: ": " :: taskName :: newLine
      }

    val targetInfoOps = moveCursorOps :: statusInfoOps :: statusBarOps
    tasks.fold(targetInfoOps) {
      _ :: _
    } :: showCursor
  }

  private def w = Config.cols
}
