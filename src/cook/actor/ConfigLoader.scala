package cook.actor

import cook.config.Config
import cook.config.ConfigRef

import scala.concurrent.Future
import scala.util.Try

trait ConfigLoader {

  def loadConfig(configRef: ConfigRef): Future[Config]
  def taskSuccess(refName: String)(tryConfig: Try[Config])
}
