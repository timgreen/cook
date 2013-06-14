package cook.actor.impl

import cook.actor.ConfigManager
import cook.actor.impl.util.BatchResponser
import cook.app.Global
import cook.config.Config
import cook.config.ConfigRef
import cook.ref.FileRef

import akka.actor.TypedActor
import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise }
import scala.util.{ Try, Success, Failure }


class ConfigManagerImpl extends ConfigManager with TypedActorBase {

  import ActorRefs._

  private val responser = new BatchResponser[String, Config]()

  private def self = configManager

  override def taskComplete(refName: String)(tryConfig: Try[Config]) {
    log.debug("configManager.taskComplete {} {}", refName, tryConfig)
    responser.complete(refName)(tryConfig)
  }

  override def getConfig(cookFileRef: FileRef): Future[Config] = {
    val refName = cookFileRef.refName
    log.debug("getConfig {}", refName)
    responser.onTask(refName) {
      doGetConfig(refName, cookFileRef)
    }
  }

  private def doGetConfig(refName: String, cookFileRef: FileRef) {
    implicit val ec = TypedActor.dispatcher

    val f = for {
      configRef <- configRefManager.getConfigRef(cookFileRef)
      config <- configLoader.loadConfig(configRef)
    } yield config

    f onComplete self.taskComplete(refName)
  }
}
