package cook.actor.impl

import cook.actor.TargetManager
import cook.actor.impl.util.BatchResponser
import cook.config.Config
import cook.ref.NativeTargetRef
import cook.ref.TargetRef
import cook.target.Target
import cook.target.TargetResult

import akka.actor.{ ActorContext, TypedActor, TypedProps }
import scala.concurrent.{ Promise, Future, Await }
import scala.concurrent.duration._


class TargetNotFoundException(val refName: String) extends RuntimeException

class TargetManagerImpl extends TargetManager with TypedActorBase {

  import ActorRefs._

  private val nativeResponser = new BatchResponser[String, Target[TargetResult]]();

  override def getTarget(targetRef: TargetRef): Future[Target[TargetResult]] = {
    log.debug("getTarget {}", targetRef.refName)
    targetRef match {
      case nativeTargetRef: NativeTargetRef =>
        nativeResponser.onTask(nativeTargetRef.cookFileRef.refName) {
          doGetNativeTarget(nativeTargetRef.refName, nativeTargetRef)
        }
    }
  }

  private def doGetNativeTarget(refName: String, nativeTargetRef: NativeTargetRef) {
    import TypedActor.dispatcher

    configManager.getConfig(nativeTargetRef.cookFileRef) flatMap { config =>
      config.getTargetOption(refName) match {
        case Some(t) =>
          Future.successful(t)
        case None =>
          Future.failed(new TargetNotFoundException(refName))
      }
    } onComplete nativeResponser.complete(refName)
  }
}
