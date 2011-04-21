package cook.app.subcommand

import java.io.File

import cook.util._
import cook.builder._

object Build extends SubCommand("build") {

  override def run(args: Array[String]) {
    if (args.isEmpty) {
      println("no target to build")
      help
    }

    // TODO(timgreen): move to better place
    val currentDir = FileUtil.relativeDirToRoot(System.getProperty("user.dir"))

    val targetLabels =
        for (a <- args) yield {
          new TargetLabel(currentDir, a)
        }

    Builder.build(targetLabels)
  }

  def help() {
    println("usage: cook build <targets>...")
  }
}
