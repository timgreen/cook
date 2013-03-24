package cook.actor

import cook.actor.util.BatchResponser
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

  private val cache = mutable.Map[String, Config]()
  private val responser = new BatchResponser[String, Config]()

  override def taskSuccess(refName: String, config: Config) {
    cache(refName) = config
    responser.success(refName, config)
  }

  override def taskFailure(refName: String, e: Throwable) {
    responser.failure(refName, e)
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
    val self = TypedActor.self[ConfigManager]

    // TODO(timgreen): use another ec?
    import scala.concurrent.ExecutionContext.Implicits.global

    val f = for {
      configRef <- configRefManager.getConfigRef(cookFileRef)
      config <- configLoader.loadConfig(configRef)
    } yield config

    f onComplete {
      case Success(config) =>
        self.taskSuccess(refName, config)
      case Failure(e) =>
        self.taskFailure(refName, e)
    }
  }
}
