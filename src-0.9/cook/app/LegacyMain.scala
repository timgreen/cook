package cook.app

import java.io.File

import org.apache.commons.cli.Options
import org.apache.commons.cli.PosixParser

import cook.app.config.Config
import cook.app.console.CookConsole
import cook.app.subcommand._
import cook.error.CookException
import cook.util.FileUtil

object Main {

  // NOTE(timgreen): this is used to break down dependances
  cook.config.runner.buildin.Installer.install
  cook.config.runner.value.methods.Installer.install

  def main(args: Array[String]) {
    init

    val options = prepareOptions
    val parser = new PosixParser();
    val commandLine = parser.parse(options, args, /* stopAtNonOption */ true)
    Config.setColumns(commandLine.getOptionValue("columns", "80"))
    val restArgs = commandLine.getArgs

    val (subCommandName, subCommandArgs) =
        if (restArgs.length == 0) {
          ("help", restArgs)
        } else {
          (restArgs.head, restArgs.drop(1))
        }

    SubCommand(subCommandName) match {
      case Some(subCommand) => {
        val exitCode = try {
          subCommand.run(subCommandArgs)
        } catch {
          case e: CookException =>
            // TODO(timgreen): stop all actor
            println(e)
            1
        }
        sys.exit(exitCode)
      }
      case None => {
        CookConsole.println("subcommand \"%s\" is not found", subCommandName)
        Help.help
        sys.exit(1)
      }
    }
  }

  private
  def init {
    findRoot
  }

  def findRoot {
    val root = FileUtil.findRootDir(new File(System.getProperty("user.dir")))
    FileUtil.setRoot(root)
    CookConsole.print("COOK_ROOT is ")
    CookConsole.control(Console.YELLOW)
    CookConsole.println(root.getAbsolutePath)
    CookConsole.reset
  }

  def prepareOptions = {
    val options = new Options();
    options.addOption("c", "columns", true, "Set console width")
    options
  }
}
