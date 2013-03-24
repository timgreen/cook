package cook.actor

import cook.config.Config
import cook.config.ConfigRef

import scala.concurrent.Future

trait ConfigLoader {

  def loadConfig(configRef: ConfigRef): Future[Config]
  def taskSuccess(refName: String, config: Config)
  def taskFailure(refName: String, e: Throwable)
}
