package cook.app.subcommand

import scala.collection.mutable.HashMap

abstract class SubCommand(val name: String, val short: String) {

  def run(args: Array[String]): Int

  def help()
}

object SubCommand {

  def apply(name: String) = get(name)

  def get(name: String) = commands.get(name)

  private[subcommand]
  val commands = HashMap[String, SubCommand](
    Help.name    -> Help,
    Build.name   -> Build,
    Clean.name   -> Clean,
    Analyze.name -> Analyze,
    Run.name     -> Run
  )
}
