package cook.app.subcommand

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
        println("subcommand \"%s\" is not found".format(args.head))
        help
        return 1
      }
    }
  }

  def help() {
    println("usage: cook <command> [<args>]")
    println("run 'cook help' to available commands")
  }

  def availableCommands {
    println("Available commands:")
    for ((name, c) <- SubCommand.commands) {
      println("%8s -- %s".format(name, c.short))
    }
  }
}
