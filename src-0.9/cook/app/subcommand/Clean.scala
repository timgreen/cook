package cook.app.subcommand

import cook.app.console.CookConsole
import cook.target._
import cook.util._

object Clean extends SubCommand("clean", "Clean up cook output") {

  override def run(args: Array[String]): Int = {
    if (args.nonEmpty) {
      // TODO(timgreen): move to better place
      val currentDir = FileUtil.relativeDirToRoot(System.getProperty("user.dir"))

      for (a <- args) {
        val targetLabel = new TargetLabel(currentDir, a)
        val target = TargetManager.getTarget(targetLabel)
        CookConsole.print("Cleaning target \"%s\" ... ", target.targetName)
        target.cleanBeforeBuild
        target.cleanBeforeExecute
        CookConsole.println("Done")
      }
    } else {
      DeleteUtil.deleteRecursively(FileUtil.cookBuildDir)
    }
    0
  }

  def help() {
    CookConsole.println("clean cook output")
    CookConsole.println("Usage: clean [<target name(s) ...>]")
  }

}
