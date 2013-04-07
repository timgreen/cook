package cook.actor

import cook.actor.util.BatchResponser
import cook.config.Config
import cook.ref.NativeTargetRef
import cook.ref.TargetRef
import cook.target.Target

import scala.concurrent.{ Promise, Future, Await }
import scala.concurrent.duration._
import akka.actor.{ ActorContext, TypedActor, TypedProps }


class TargetNotFoundException(val refName: String) extends RuntimeException

class TargetManagerImpl extends TargetManager with TypedActorBase {

  private val nativeResponser = new BatchResponser[String, Target[_]]();

  override def getTarget(targetRef: TargetRef): Future[Target[_]] = {
    targetRef match {
      case nativeTargetRef: NativeTargetRef =>
        nativeResponser.onTask(nativeTargetRef.cookFileRef.refName) {
          doGetNativeTarget(nativeTargetRef.cookFileRef.refName, nativeTargetRef)
        }
    }
  }

  private def doGetNativeTarget(refName: String, nativeTargetRef: NativeTargetRef) {
    // TODO(timgreen): use another ec?
    import scala.concurrent.ExecutionContext.Implicits.global

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
