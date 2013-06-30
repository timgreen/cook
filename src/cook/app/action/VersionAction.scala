package cook.app.action

import cook.app.MainHandler
import cook.app.version.Version
import cook.console.Console

object VersionAction {

  def run {
    Console.printVersion(Version.version)
    MainHandler.exec()
  }
}
