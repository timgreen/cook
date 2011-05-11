package cook.app.subcommand

object Help extends SubCommand("help", "Show help message") {

  override def run(args: Array[String]) {
    if (args.isEmpty) {
      availableCommands
    } else {
      SubCommand(args.head) match {
        case Some(subCommand) => subCommand.help
        case None => {
          println("subcommand \"%s\" is not found".format(args.head))
          help
        }
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
