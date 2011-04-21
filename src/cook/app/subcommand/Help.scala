package cook.app.subcommand

class Help extends SubCommand("help") {

  override def run(args: Array[String]) {
    if (args.isEmpty) {
      help
    }
  }

  def help() {
    println("usage: cook [--version] [--help]")
    println("<command> [<args>]")
  }
}
