package cook.actor

import cook.config.ConfigRef
import cook.ref.FileRef

import scala.concurrent.Future

trait ConfigRefManager {

  def getConfigRef(cookFileRef: FileRef): Future[ConfigRef]
}
