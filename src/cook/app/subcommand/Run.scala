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
    if (args.size > 1) {
      CookConsole.println("can only run one target at a time")
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
    Runner.run(targetLabel)

    0
  }

  def help() {
    CookConsole.println("usage: cook run <target>")
  }
}
