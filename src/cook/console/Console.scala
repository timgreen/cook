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

  object style {
    val done   = green :: bold
    val cached = yellow :: bold
    val building = cyan :: bold
    val pending = blue :: bold :: underlined
    val unsolved = bold
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
        blink("Finding") :: " " :: cyan(total.toString)    :: " target(s): " ::
        style.done("Done")         :: " " :: done.toString     :: " " ::
        style.cached("Cached")     :: " " :: cached.toString   :: " " ::
        style.building("Building") :: " " :: building.toString :: " " ::
        style.pending("Pending")   :: " " :: pending.toString  :: " " ::
        style.unsolved("Unsolved") :: " " :: unsolved.toString        ::
        newLine
      } else {
        "Found " :: cyan(total.toString)  :: " target(s): "           ::
        style.done("Done")         :: " " :: done.toString     :: " " ::
        style.cached("Cached")     :: " " :: cached.toString   :: " " ::
        style.building("Building") :: " " :: building.toString :: " " ::
        style.pending("Pending")   :: " " :: pending.toString  :: " " ::
        newLine
      }

      // draw progress bar
      val barWidth      = w - 4
      val doneWidth     = barWidth * done / total
      val cachedWidth   = barWidth * cached / total
      val buildingWidth = barWidth * building / total
      val unsolvedWidth = barWidth * unsolved / total
      val pendingWidth  = barWidth - doneWidth - cachedWidth - buildingWidth - unsolvedWidth

      val cookLen = doneWidth + cachedWidth + buildingWidth
      val cookStr = "C" + "o" * (cookLen - 2) + "k"

      val doneStr     = cookStr.take(doneWidth)
      val cachedStr   = cookStr.substring(doneWidth).take(cachedWidth)
      val buildingStr = cookStr.substring(doneWidth + cachedWidth).take(buildingWidth)
      val pendingStr  = " " * pendingWidth
      val unsolvedStr = " " * unsolvedWidth

      val statusBarOps = "[ "         ::
        style.done     :: doneStr     ::
        style.cached   :: cachedStr   ::
        style.building :: buildingStr ::
        style.pending  :: pendingStr  ::
        style.unsolved :: unsolvedStr ::
        reset :: " ]" :: newLine

      val tasksOps =
        taskInfo.toList.sorted.map { case (taskType, taskName) =>
          cyan(taskType) :: ": " :: taskName :: newLine
        }

      moveCursorOps :: statusInfoOps :: statusBarOps :: tasksOps ::: showCursor
    }

    lineUsed = s.lineUsed
  }

  def runTarget(targetRefName: String) = print {
    "Run Target: " :: strong(targetRefName)
  }

  private def w = Config.cols
}
