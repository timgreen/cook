package cook.actor.impl

import cook.actor.ConfigRefVerifier
import cook.config.ConfigRef
import cook.ref.FileRef

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Try, Success, Failure }

class ConfigRefVerifierImpl extends ConfigRefVerifier with TypedActorBase {

  import ActorRefs._

  private val passVerifySet = mutable.Set[String]()

  override def passCycleCheck(configRef: ConfigRef): Future[Try[Boolean]] = Future successful {
    if (passVerifySet.contains(configRef.fileRef.refName)) {
      Success(true)
    } else {
      Try { doCycleCheck(Set(), configRef) }
    }
  }

  private def doCycleCheck(traceSet: Set[String], ref: ConfigRef): Boolean = {
    val refName = ref.fileRef.refName
    if (passVerifySet.contains(refName)) {
      true
    } else if (traceSet.contains(refName)) {
      false
    } else {
      val nextTraceSet = traceSet + refName
      val fRefs = ref.includes map { d =>
        configRefLoader.loadConfigRef(d.ref)
      }

      // TODO(timgreen): use another ec?
      import scala.concurrent.ExecutionContext.Implicits.global
      val refs = Await.result(Future.sequence(fRefs), Duration.Inf)
      val foundCycle = refs find { ref =>
        !doCycleCheck(nextTraceSet, ref)
      } isDefined

      passVerifySet += refName
      !foundCycle
    }
  }
}
