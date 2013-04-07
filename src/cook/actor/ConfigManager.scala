package cook.actor

import cook.config.Config
import cook.ref.FileRef

import scala.concurrent.Future
import scala.util.Try

trait ConfigManager {

  def getConfig(cookFileRef: FileRef): Future[Config]
  def taskComplete(refName: String)(tryConfig: Try[Config])
}
