package cook.app.subcommand

abstract class SubCommand(val name: String) {

  def run(args: Array[String])

  def help()
}

object SubCommand {

}
