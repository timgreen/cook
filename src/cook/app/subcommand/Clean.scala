package cook.app.subcommand

import java.io.File

import cook.app.console.CookConsole
import cook.util._

object Clean extends SubCommand("clean", "Clean up cook output") {

  override def run(args: Array[String]): Int = {
    deleteRecursively(FileUtil.cookBuildDir)
    0
  }

  def help() {
    CookConsole.println("clean cook output")
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
