package cook.app

import cook.app.console.CookConsole
import cook.path.Path

import scala.tools.nsc.io.{ Path => SPath, Directory }

object Main {

  private var _config: Config = _
  def config = _config

  def main(args: Array[String]) {
    val parser = new CookOptionParser(args)
    _config = Config(
      cols = parser.cols(),
      parallel = parser.parallel()
    )
    val path = findAndPrintRootDir
    parser.subcommand match {
      case None =>
        parser.showUsage
      case Some(parser.commandBuild) =>
        println("build " + parser.commandBuild.targets())
      case Some(parser.commandClean) =>
        println("clean " + parser.commandClean.targets())
      case Some(parser.commandRun) =>
        println("run " + parser.commandRun.target())
      case Some(parser.commandAnalyze) =>
        println("analyze " + parser.commandAnalyze.target())
      case Some(parser.commandHelp) =>
        parser.printHelp
    }
  }

  def findAndPrintRootDir: Path = {
    val path = Path(Directory.Current)
    CookConsole.printRootDir(path.rootDir.toString)
    path
  }
}
