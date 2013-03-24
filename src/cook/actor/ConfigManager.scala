package cook.actor

import cook.config.Config
import cook.ref.FileRef

import scala.concurrent.Future

trait ConfigManager {

  def getConfig(cookFileRef: FileRef): Future[Config]
  def taskSuccess(refName: String, config: Config)
  def taskFailure(refName: String, e: Throwable)
}
