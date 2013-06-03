package cook.app

import cook.app.action._
import cook.app.console.CookConsole
import cook.path.Path
import cook.ref.RefFactoryRegister

import scala.reflect.io.{ Path => SPath, Directory }

object Main {

  def main(args: Array[String]) {
    val parser = new CookOptionParser(args)

    Config.cols = parser.cols()
    Config.cliMaxJobs = parser.maxJobs.get

    try {
      findAndPrintRootDir
      RefFactoryRegister.init
      loadCookRootConfig
      runSubCommand(parser)
    } catch {
      case e: Throwable =>
        MainHandler.handleException(e)
    }
  }

  def runSubCommand(parser: CookOptionParser) {
    parser.subcommand match {
      case None =>
        parser.showUsage
      case Some(parser.commandBuild) =>
        println("build " + parser.commandBuild.targets())
        BuildAction.run(parser.commandBuild.targets())
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

  def loadCookRootConfig {
    import cook.ref.{RefManager, Ref, FileRef}
    import com.typesafe.config.{ConfigFactory, ConfigParseOptions}

    val cookRootConfigFile = RefManager(Nil, "/COOK_ROOT").as[FileRef]
    val config = ConfigFactory.parseFile(
      cookRootConfigFile.toPath.jfile,
      ConfigParseOptions.defaults.setAllowMissing(false)
    )

    Config.setConf(config)
  }
}
