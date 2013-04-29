package cook.actor.impl

import cook.actor.ConfigRefManager
import cook.actor.impl.util.BatchResponser
import cook.app.Global
import cook.config.ConfigRef
import cook.ref.FileRef

import scala.concurrent.{ Future, Promise, Await }
import scala.concurrent.duration._
import scala.collection.mutable
import scala.util.{ Try, Success, Failure }
import akka.actor.{ TypedActor, TypedProps }

object ConfigRefVerifyTask {

  def apply(refName: String)(runBlock: => Unit): Runnable = new Runnable {
    override def run() {
      runBlock
    }
  }
}

class ConfigRefManagerImpl extends ConfigRefManager with TypedActorBase {

  private val responser = new BatchResponser[String, ConfigRef]()

  override def getConfigRef(cookFileRef: FileRef): Future[ConfigRef] = {
    val refName = cookFileRef.refName
    responser.onTask(refName) {
      doGetConfigRef(refName, cookFileRef)
    }
  }

  override def taskComplete(refName: String)(tryConfigRef: Try[ConfigRef]) {
    responser.complete(refName)(tryConfigRef)
  }

  private def doGetConfigRef(refName: String, cookFileRef: FileRef) {
    val self = TypedActor.self[ConfigRefManager]
    // TODO(timgreen): use another ec?
    import scala.concurrent.ExecutionContext.Implicits.global
    configRefLoader.loadConfigRef(cookFileRef) onComplete {
      case Success(configRef) =>
        Global.configRefVerifyDispatcher.execute(ConfigRefVerifyTask(refName) {
          Await.result(configRefVerifier.passCycleCheck(configRef), Duration.Inf) match {
            case Success(true) =>
              self.taskComplete(refName)(Success(configRef))
            case Success(false) =>
              // TODO(timgreen): throw cycleFound exception
              // self.taskComplete(refName)(...)
            case Failure(e) =>
              self.taskComplete(refName)(Failure(e))
          }
        })
      case Failure(e) =>
        self.taskComplete(refName)(Failure(e))
    }
  }
}
