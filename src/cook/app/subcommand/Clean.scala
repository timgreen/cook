package cook.app.subcommand

import java.io.File

import org.apache.tools.ant.taskdefs.Delete

import cook.util._

object Clean extends SubCommand("clean", "Clean up cook output") {

  override def run(args: Array[String]): Int = {
    removeDir(FileUtil.cookBuildDir)
    0
  }

  def help() {
    println("clean cook output")
  }

  private[subcommand]
  def removeDir(dir: File) {
    val deleteTask = new Delete
    deleteTask.setQuiet(true)
    deleteTask.setDir(dir)
    deleteTask.execute
  }
}
