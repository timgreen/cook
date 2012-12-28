package cook.app

import cook.app.console.CookConsole
import cook.path.Path
import cook.ref.RefFactoryRegister

import scala.tools.nsc.io.{ Path => SPath, Directory }

object Main {

  def main(args: Array[String]) {
    val parser = new CookOptionParser(args)

    Config.cols = parser.cols()
    Config.parallel = parser.parallel()

    findAndPrintRootDir
    RefFactoryRegister.init
    parseAndLoadCookRoot

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

  def findAndPrintRootDir {
    CookConsole.printRootDir(Path(Directory.Current).rootDir.toString)
  }

  def parseAndLoadCookRoot {
    CookConsole.updateCookRootLoadingStatus()
    // TODO(timgreen):
    CookConsole.clearCookRootLoadingStatus
  }
}
