package cook.app

import cook.app.subcommand._

object Main {

  def main(args: Array[String]) {
    val subCommandName =
        if (args.length == 0) {
          "help"
        } else {
          args(0)
        }

    SubCommand(subCommandName) match {
      case Some(subCommand) => subCommand.run(args)
      case None => println("subcommand \"%s\" is not found".format(subCommandName))
    }
  }
}

