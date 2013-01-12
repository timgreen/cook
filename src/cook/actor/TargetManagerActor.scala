package cook.actor

import cook.actor.util.BatchResponser
import cook.config.Config
import cook.ref.NativeTargetRef
import cook.ref.TargetRef

import akka.actor.ActorRef
import scala.collection.mutable

class TargetManagerActor extends ActorBase {

  private val nativeResponser = new BatchResponser[String, (ActorRef, NativeTargetRef)]();

  val configManagerActor = context.actorFor("./ConfigManager")

  def receive = {
    case GetTarget(targetRef) =>
      targetRef match {
        case nativeTargetRef: NativeTargetRef =>
          nativeResponser.onTask(nativeTargetRef.cookFileRef.refName, sender -> nativeTargetRef) {
            configManagerActor ! GetConfig(nativeTargetRef.cookFileRef)
          }
        case _ =>
          // TODO(timgreen):
      }
    case config: Config =>
      nativeResponser.complete(config.refName) { case (s, ref) =>
        s ! config.getTargetOption(ref.targetName)
      }
  }
}
