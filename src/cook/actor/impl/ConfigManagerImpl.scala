package cook.actor.impl

import cook.actor.ConfigManager
import cook.actor.impl.util.BatchResponser
import cook.app.Global
import cook.config.Config
import cook.config.ConfigRef
import cook.ref.FileRef

import akka.actor.TypedActor
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise }
import scala.util.{ Try, Success, Failure }

object ConfigGetTask {

  def apply(refName: String)(runBlock: => Unit): Runnable = new Runnable {
    override def run() {
      runBlock
    }
  }
}

class ConfigManagerImpl extends ConfigManager with TypedActorBase {

  import ActorRefs._

  private val cache = mutable.Map[String, Config]()
  private val responser = new BatchResponser[String, Config]()

  private def self = configManager

  override def taskComplete(refName: String)(tryConfig: Try[Config]) {
    tryConfig match {
      case Success(config) =>
        cache(refName) = config
        responser.success(refName, config)
      case Failure(e) =>
        responser.failure(refName, e)
    }
  }

  override def getConfig(cookFileRef: FileRef): Future[Config] = {
    val refName = cookFileRef.refName
    cache.get(refName) match {
      case Some(config) =>
        Future.successful(config)
      case None =>
        responser.onTask(refName) {
          doGetConfig(refName, cookFileRef)
        }
    }
  }

  private def doGetConfig(refName: String, cookFileRef: FileRef) {
    import TypedActor.dispatcher

    val f = for {
      configRef <- configRefManager.getConfigRef(cookFileRef)
      config <- configLoader.loadConfig(configRef)
    } yield config

    f onComplete self.taskComplete(refName)
  }
}
