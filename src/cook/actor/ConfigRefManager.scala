package cook.actor

import cook.config.ConfigRef
import cook.ref.FileRef

import scala.concurrent.Future
import scala.util.Try

trait ConfigRefManager {

  def getConfigRef(cookFileRef: FileRef): Future[ConfigRef]

  def taskComplete(refName: String)(tryConfigRef: Try[ConfigRef])
}
