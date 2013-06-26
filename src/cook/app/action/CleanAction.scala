package cook.app.action

import cook.app.MainHandler
import cook.path.Path

object CleanAction {

  def run {
    MainHandler.exec()
    Path().cookWorkspaceDir.deleteRecursively
  }
}
