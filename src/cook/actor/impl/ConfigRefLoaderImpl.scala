package cook.actor.impl

import cook.actor.ConfigRefLoader
import cook.actor.impl.util.BatchResponser
import cook.actor.impl.util.TaskBuilder
import cook.app.Global
import cook.config.ConfigRef
import cook.error._
import cook.ref.FileRef

import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration._
import scala.util.{ Try, Success, Failure }
import akka.actor.TypedActor

object ConfigRefLoadTask extends TaskBuilder("ConfigRefLoad")

class ConfigRefLoaderImpl extends ConfigRefLoader with TypedActorBase {

  import ActorRefs._

  private val responser = new BatchResponser[String, ConfigRef](processError)

  private def self = configRefLoader

  private def processError(key: String, e: Throwable): Throwable = error(e) {
    import cook.console.ops._
    "Error when reading config: " :: strong(key)
  }

  override def taskComplete(refName: String)(tryConfigRef: Try[ConfigRef]) {
    responser.complete(refName)(tryConfigRef)
  }

  override def loadConfigRef(cookFileRef: FileRef): Future[ConfigRef] = {
    val refName = cookFileRef.refName
    log.debug("loadConfigRef {}", refName)
    responser.onTask(refName) {
      doLoadConfigRef(refName, cookFileRef)
    }
  }

  private def doLoadConfigRef(refName: String, cookFileRef: FileRef) {
    Global.workerDispatcher.execute(ConfigRefLoadTask(refName) {
      self.taskComplete(refName)(Try(new ConfigRef(cookFileRef)))
    })
  }
}
