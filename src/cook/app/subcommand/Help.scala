package cook.app.subcommand

import cook.app.console.CookConsole

object Help extends SubCommand("help", "Show help message") {

  override def run(args: Array[String]): Int = {
    if (args.isEmpty) {
      availableCommands
      return 0
    }

    SubCommand(args.head) match {
      case Some(subCommand) => {
        subCommand.help
        return 0
      }
      case None => {
        CookConsole.println("subcommand \"%s\" is not found".format(args.head))
        help
        return 1
      }
    }
  }

  def help() {
    CookConsole.println("usage: cook <command> [<args>]")
    CookConsole.println("run 'cook help' to available commands")
  }

  def availableCommands {
    CookConsole.println("Available commands:")
    for ((name, c) <- SubCommand.commands) {
      CookConsole.println("%8s -- %s", name, c.short)
    }
  }
}
