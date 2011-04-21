package cook.app.subcommand

object Help extends SubCommand("help") {

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
