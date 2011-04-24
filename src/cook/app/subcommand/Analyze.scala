package cook.app.subcommand

import java.io.File

import cook.util._
import cook.actors._

// https://chart.googleapis.com/chart?cht=gv:neato&chl=digraph{A->B;B->C;}
object Analyze extends SubCommand("analyze") {

  override def run(args: Array[String]) {
    if (args.isEmpty) {
      println("no target to analyze")
      help
      return
    }
    if (args.length > 1) {
      println("Can only analyze one target at one time for now")
      help
      return
    }

    // TODO(timgreen): move to better place
    val currentDir = FileUtil.relativeDirToRoot(System.getProperty("user.dir"))
    val targetLabel = new TargetLabel(currentDir, args(0))

    Analysis.analyze(targetLabel)
  }

  def help() {
    println("usage: cook analyze <target>")
  }
}
