package cook.actor

import cook.actor.util.BatchResponser
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

  override def getSuccess(refName: String, configRef: ConfigRef) {
    responser.success(refName, configRef)
  }
  override def getFailure(refName: String, e: Throwable) {
    responser.failure(refName, e)
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
              self.getSuccess(refName, configRef)
            case Success(false) =>
              // TODO(timgreen): throw cycleFound exception
              // self.getFailure(refName, )
            case Failure(e) =>
              self.getFailure(refName, e)
          }
        })
      case Failure(failure) =>
        self.getFailure(refName, failure)
    }
  }
}
