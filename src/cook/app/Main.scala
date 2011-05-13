package cook.app

import java.io.File

import cook.app.subcommand._
import cook.util.FileUtil

object Main {

  // NOTE(timgreen): this is used to break down dependances
  cook.config.runner.buildin.Installer.install
  cook.config.runner.value.methods.Installer.install

  def main(args: Array[String]) {
    init

    val (subCommandName, subCommandArgs) =
        if (args.length == 0) {
          ("help", args)
        } else {
          (args.head, args.drop(1))
        }

    SubCommand(subCommandName) match {
      case Some(subCommand) => subCommand.run(subCommandArgs)
      case None => {
        println("subcommand \"%s\" is not found".format(subCommandName))
        Help.help
      }
    }
  }

  def init {
    findRoot
  }

  def findRoot {
    val root = FileUtil.findRootDir(new File(System.getProperty("user.dir")))
    FileUtil.setRoot(root)
    println("COOK_ROOT dir is " + root.getAbsolutePath)
  }
}

