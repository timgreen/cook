package cook.app

import cook.app.action._
import cook.console.Console
import cook.path.Path
import cook.ref.RefFactoryRegister

import scala.reflect.io.{ Path => SPath, Directory }

object Main {

  def main(args: Array[String]) {
    val parser = new CookOptionParser(args)

    Config.cols = parser.cols()

    RefFactoryRegister.init
    findAndPrintRootDir
    loadCookRootConfig
    try {
      MainHandler.prepareMetaDb
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
        BuildAction.run(parser.commandBuild.targets())
      case Some(parser.commandClean) =>
        println("clean " + parser.commandClean.targets())
      case Some(parser.commandRun) =>
        RunAction.run(parser.commandRun.target() :: Nil)
      case Some(parser.commandAnalyze) =>
        println("analyze " + parser.commandAnalyze.target())
      case Some(parser.commandHelp) =>
        parser.printHelp
    }
  }

  def findAndPrintRootDir {
    try {
      Console.printRootDir(Path(Directory.Current).rootDir.toString)
    } catch {
      case e: Throwable =>
        Console.cookRootNotFound
        sys.exit(1)
    }
  }

  def loadCookRootConfig {
    import cook.ref.{RefManager, Ref, FileRef}
    import com.typesafe.config.{ConfigFactory, ConfigParseOptions}

    try {
      val cookRootConfigFile = RefManager(Nil, "/COOK_ROOT").as[FileRef]
      val config = ConfigFactory.parseFile(
        cookRootConfigFile.toPath.jfile,
        ConfigParseOptions.defaults.setAllowMissing(false)
      )

      Config.setConf(config)
    } catch {
      case e: com.typesafe.config.ConfigException =>
        Console.CookRootFormatError(e.getMessage)
        sys.exit(1)
    }
  }
}
