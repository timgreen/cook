package cook.actor

import cook.config.ConfigRef
import cook.ref.FileRef

import scala.concurrent.Future

trait ConfigRefLoader {

  def loadConfigRef(cookFileRef: FileRef): Future[ConfigRef]
  def cacheConfigRef(refName: String, configRef: ConfigRef)
}
