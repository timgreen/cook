package cook.actor

import cook.actor.util.BatchResponser
import cook.config.ConfigRef
import cook.ref.FileRef

import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration._
import scala.collection.mutable
import akka.actor.TypedActor

object ConfigRefLoadTask {

  def apply(refName: String)(runBlock: => Unit): Runnable = new Runnable {
    override def run() {
      runBlock
    }
  }
}

class ConfigRefLoaderImpl extends ConfigRefLoader {

  private val cache = mutable.Map[String, ConfigRef]()
  private val responser = new BatchResponser[String, ConfigRef]()

  override def cacheConfigRef(refName: String, configRef: ConfigRef) {
    cache(refName) = configRef
  }

  override def loadConfigRef(cookFileRef: FileRef): Future[ConfigRef] = {
    val refName = cookFileRef.refName
    cache.get(refName) match {
      case Some(configRef) =>
        Future.successful(configRef)
      case None =>
        val f = responser.onTask(refName) { p =>
          doLoadConfigRef(refName, cookFileRef, p)
        }
        // TODO(timgreen): use another ec?
        import scala.concurrent.ExecutionContext.Implicits.global
        f onSuccess { case configRef =>
          TypedActor.self[ConfigRefLoader].cacheConfigRef(refName, configRef)
        }
        f
    }
  }

  private def doLoadConfigRef(refName: String, cookFileRef: FileRef, p: Promise[ConfigRef]) {
    val workerExecutionContext = TypedActor.context.system.dispatchers.lookup("worker-dispatcher")
    workerExecutionContext.execute(ConfigRefLoadTask(refName) {
      val ref = new ConfigRef(cookFileRef)
      p.success(ref)
    })
  }
}
