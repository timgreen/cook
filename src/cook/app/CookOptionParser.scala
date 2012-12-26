package cook.app

import org.rogach.scallop.ScallopConf
import org.rogach.scallop.Subcommand
import org.rogach.scallop.exceptions._

class CookOptionParser(args: Seq[String]) extends ScallopConf(args) {
  version("version 1.0")
  banner("Cook [options] <sub command> [target(s) ...]")

  val cols = opt[Int]("cols", 'w', descr = "Terminal width", default = Some(80))
  val parallel = opt[Int]("parallel", 'p', descr = "Parallel workers",
    default = Some(sys.runtime.availableProcessors))

  val commandBuild = new Subcommand("build") {
    val targets = trailArg[List[String]]("targets")
  }

  val commandClean = new Subcommand("clean") {
    val targets = trailArg[List[String]]("targets", required = false)
  }

  val commandRun = new Subcommand("run") {
    val target = trailArg[String]("target")
  }

  val commandAnalyze = new Subcommand("analyze") {
    val target = trailArg[String]("target")
  }

  val commandHelp = new Subcommand("help")

  verify

  override def onError(e: Throwable) {
    if (e.isInstanceOf[ScallopException]) {
      showUsage
    }
    super.onError(e)
  }

  def showUsage {
    println("""
    clean -- Clean up cook output
  analyze -- Analyze target dependances
      help -- Show help message
      run -- Run target
    build -- Build targets
      """)
  }

}
