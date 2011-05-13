package cook.app.subcommand

import cook.util._
import cook.actors._

object Run extends SubCommand("run", "Run target") {

  override def run(args: Array[String]): Int = {
    if (args.isEmpty) {
      println("no target to run")
      help
      return 1
    }
    if (args.size > 1) {
      println("can only run one target at a time")
      help
      return 1
    }

    // TODO(timgreen): move to better place
    val currentDir = FileUtil.relativeDirToRoot(System.getProperty("user.dir"))
    val targetLabel = new TargetLabel(currentDir, args(0))

    Builder.build(Seq(targetLabel))
    Runner.run(targetLabel)

    0
  }

  def help() {
    println("usage: cook run <target>")
  }
}
