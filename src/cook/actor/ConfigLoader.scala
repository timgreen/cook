package cook.actor

import cook.config.Config
import cook.config.ConfigRef

import scala.concurrent.Future
import scala.util.Try

case class LoadConfigClassTaskInfo(
  configRef: ConfigRef,
  depConfigRefs: List[ConfigRef]
)

trait ConfigLoader {

  def loadConfig(configRef: ConfigRef): Future[Config]
  def taskComplete(refName: String)(tryConfig: Try[Config])

  def step2WaitDepConfig(configRef: ConfigRef)(tryDepConfigRefs: Try[List[ConfigRef]])
  def step3LoadConfigClass(taskInfo: LoadConfigClassTaskInfo)
}
