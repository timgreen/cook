package cook.app.subcommand

import scala.collection.immutable.VectorBuilder

import java.io.File

import cook.actors._
import cook.target._
import cook.util._

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

    val targets = Analysis.analyze(targetLabel)
    val digraph = "digraph{%s}".format(buildDigraph(targets).mkString(";"))

    // TODO(timgreen): support more output, like graphviz to svg
    println("https://chart.googleapis.com/chart?cht=gv:neato&chl=%s".format(digraph))
  }

  def help() {
    println("usage: cook analyze <target>")
  }

  private[subcommand]
  def buildDigraph(targets: Seq[Target]): Seq[String] = {
    val digraphBuilder = new VectorBuilder[String]

    for (t <- targets) {
      for (dep <- t.depTargets) {
        val link = "\"%s\"->\"%s\"".format(t.targetName, dep.targetName)
        digraphBuilder += link
      }
    }

    digraphBuilder.result
  }
}
