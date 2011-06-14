package cook.app.subcommand

import java.io.File

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

        deleteRecursively(target.outputDir)
        deleteRecursively(target.buildLogFile)
        deleteRecursively(target.buildShFile)
        deleteRecursively(target.runLogFile)
        deleteRecursively(target.runShFile)

        CookConsole.println("Done")
      }
    } else {
      deleteRecursively(FileUtil.cookBuildDir)
    }
    0
  }

  def help() {
    CookConsole.println("clean cook output")
    CookConsole.println("Usage: clean [<target name(s) ...>]")
  }

  private[subcommand]
  def deleteRecursively(f: File): Boolean = {
    if (!isSymlink(f) && f.isDirectory) f.listFiles match {
      case null =>
      case xs   => xs foreach deleteRecursively
    }
    f.delete
  }

  def isSymlink(f: File): Boolean = {
    f.getCanonicalPath != f.getAbsolutePath
  }
}
