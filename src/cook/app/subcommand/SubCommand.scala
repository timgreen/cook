package cook.app.subcommand

import scala.collection.mutable.HashMap

abstract class SubCommand(val name: String) {

  def run(args: Array[String])

  def help()
}

object SubCommand {

  def apply(name: String) = get(name)

  def get(name: String) = commands.get(name)

  private[subcommand]
  val commands = HashMap[String, SubCommand](
    "help"  -> Help,
    "build" -> Build,
    "clean" -> Clean
  )
}
