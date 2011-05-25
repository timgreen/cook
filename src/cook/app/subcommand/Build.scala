package cook.app.subcommand

import cook.actors._
import cook.app.console.CookConsole
import cook.util._

object Build extends SubCommand("build", "Build targets") {

  override def run(args: Array[String]): Int = {
    if (args.isEmpty) {
      CookConsole.println("no target to build")
      help
      return 1
    }

    // TODO(timgreen): move to better place
    val currentDir = FileUtil.relativeDirToRoot(System.getProperty("user.dir"))

    val targetLabels =
        for (a <- args) yield {
          new TargetLabel(currentDir, a)
        }

    Builder.build(targetLabels)

    0
  }

  def help() {
    println("usage: cook build <targets>...")
  }
}
