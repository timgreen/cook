package cook.app.subcommand

import scala.collection.immutable.VectorBuilder
import scala.collection.mutable.HashSet

import java.io.File

import cook.app.console.CookConsole
import cook.actors._
import cook.target._
import cook.util._

object Analyze extends SubCommand("analyze", "Analyze target dependances") {

  override def run(args: Array[String]): Int = {
    if (args.isEmpty) {
      CookConsole.println("no target to analyze")
      help
      return 1
    }
    if (args.length > 1) {
      CookConsole.println("Can only analyze one target at one time for now")
      help
      return 1
    }

    // TODO(timgreen): move to better place
    val currentDir = FileUtil.relativeDirToRoot(System.getProperty("user.dir"))
    val targetLabel = new TargetLabel(currentDir, args(0))

    val targets = new VectorBuilder[Target]
    val targetSet= new HashSet[String]

    val analyst = Analyst(targetLabel)
    while (analyst.nonEmpty) {
      val t = analyst.get.get
      analyst.setBuilt(t)
      if (!targetSet.contains(t)) {
        targets += TargetManager.getTarget(t)
      }
    }

    val digraph = "digraph{%s}".format(buildDigraph(targets.result).mkString(";"))

    // TODO(timgreen): support more output, like graphviz to svg
    CookConsole.println("https://chart.googleapis.com/chart?cht=gv:neato&chl=%s", digraph)

    0
  }

  def help() {
    CookConsole.println("usage: cook analyze <target>")
  }

  private[subcommand]
  def buildDigraph(targets: Seq[Target]): Seq[String] = {
    val digraphBuilder = new VectorBuilder[String]

    for (t <- targets) {
      for (dep <- t.deps) {
        val link = "\"%s\"->\"%s\"".format(t.targetName, dep.targetName)
        digraphBuilder += link
      }
    }

    digraphBuilder.result
  }
}
