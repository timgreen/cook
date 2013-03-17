package cook.actor

import cook.actor.util.BatchResponser
import cook.config.ConfigRef
import cook.ref.FileRef

import scala.concurrent.{ Future, Promise, Await }
import scala.concurrent.duration._
import scala.collection.mutable
import scala.util.{ Success, Failure }
import akka.actor.{ TypedActor, TypedProps }

object ConfigRefVerifyTask {

  def apply(refName: String)(runBlock: => Unit): Runnable = new Runnable {
    override def run() {
      runBlock
    }
  }
}

class ConfigRefManagerImpl extends ConfigRefManager {

  private val responser = new BatchResponser[String, ConfigRef]()

  override def getConfigRef(cookFileRef: FileRef): Future[ConfigRef] = {
    val refName = cookFileRef.refName
    responser.onTask(refName) { p =>
      doGetConfigRef(refName, cookFileRef, p)
    }
  }

  private def configRefLoader = {
    val system = TypedActor.context.system
    TypedActor(system).typedActorOf(
      TypedProps[ConfigRefLoader],
      system.actorFor("ConfigRefLoader"))
  }

  private def doGetConfigRef(refName: String, cookFileRef: FileRef, p: Promise[ConfigRef]) {
    // TODO(timgreen): use another ec?
    import scala.concurrent.ExecutionContext.Implicits.global
    configRefLoader.loadConfigRef(cookFileRef) onComplete {
      case Success(configRef) =>
        val ec = TypedActor.context.system.dispatchers.lookup("configref-verify-worker-dispatcher")
        ec.execute(ConfigRefVerifyTask(refName) {
          if (passCycleCheck(refName, configRef)) {
            p.success(configRef)
          } else {
            // TODO(timgreen):
            // p.failure()
          }
        })
      case Failure(failure) =>
        // TODO(timgreen):
        p.failure(failure)
    }
  }

  private val passVerifySet = mutable.Set[String]()

  private def passCycleCheck(refName: String, configRef: ConfigRef): Boolean = {
    if (passVerifySet.contains(refName)) {
      true
    } else {
      val trace = mutable.Set[String]()
      doCycleCheck(trace, configRef)
    }
  }

  private def doCycleCheck(trace: mutable.Set[String], ref: ConfigRef): Boolean = {
    if (passVerifySet.contains(ref.fileRef.toPath.path)) {
      true
    } else if (trace.contains(ref.fileRef.toPath.path)) {
      false
    } else {
      trace += ref.fileRef.toPath.path
      val fRefs = ref.imports map { d =>
        configRefLoader.loadConfigRef(d.ref)
      }

      // TODO(timgreen): use another ec?
      import scala.concurrent.ExecutionContext.Implicits.global
      val refs = Await.result(Future.sequence(fRefs), Duration.Inf)
      val foundCycle = refs find { ref =>
        !doCycleCheck(trace, ref)
      } isDefined

      trace -= ref.fileRef.refName
      passVerifySet += ref.fileRef.refName
      !foundCycle
    }
  }
}
