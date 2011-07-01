package cook.app.subcommand

import cook.actors._
import cook.app.console.CookConsole
import cook.util._

object Run extends SubCommand("run", "Run target") {

  override def run(args: Array[String]): Int = {
    if (args.isEmpty) {
      CookConsole.println("no target to run")
      help
      return 1
    }

    // TODO(timgreen): move to better place
    val currentDir = FileUtil.relativeDirToRoot(System.getProperty("user.dir"))
    val targetLabel = new TargetLabel(currentDir, args(0))

    val buildExitCode = Builder.build(Seq(targetLabel))
    if (buildExitCode != 0) {
      return buildExitCode
    }
    Runner.run(targetLabel, args.drop(1))

    0
  }

  def help() {
    CookConsole.println("usage: cook run <target>")
  }
}
