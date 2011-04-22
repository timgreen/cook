package cook.app

import cook.app.subcommand._

object Main {

  def main(args: Array[String]) {
    val (subCommandName, subCommandArgs) =
        if (args.length == 0) {
          ("help", args)
        } else {
          (args(0), args.drop(1))
        }

    SubCommand(subCommandName) match {
      case Some(subCommand) => subCommand.run(subCommandArgs)
      case None => println("subcommand \"%s\" is not found".format(subCommandName))
    }
  }
}

