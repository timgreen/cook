package cook.actor

import cook.config.ConfigRef
import cook.ref.FileRef

import scala.concurrent.Future
import scala.util.Try

trait ConfigRefManager {

  def taskComplete(refName: String)(tryConfigRef: Try[ConfigRef])
  def getConfigRef(cookFileRef: FileRef): Future[ConfigRef]
  def step2LoadIncludeRefs(refName: String, configRef: ConfigRef)
  def checkDag
}
