package cook.actor

import cook.config.Config
import cook.ref.TargetRef
import cook.ref.NativeTargetRef

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable

class TargetManagerActor extends Actor {

  val configManagerActor = context.actorFor("./ConfigManager")
  private val nativeTargetWaiters = mutable.Map[String, mutable.ListBuffer[(ActorRef, NativeTargetRef)]]()

  def receive = {
    case GetTarget(targetRef) =>
      targetRef match {
        case nativeTargetRef: NativeTargetRef =>
          val list = nativeTargetWaiters.getOrElseUpdate(
            nativeTargetRef.cookFileRef.refName, mutable.ListBuffer[(ActorRef, NativeTargetRef)]())
          list += (sender -> nativeTargetRef)
          if (list.size == 1) {
            configManagerActor ! GetConfig(nativeTargetRef.cookFileRef)
          }
        case _ =>
          // TODO(timgreen):
      }
    case config: Config =>
      nativeTargetWaiters.remove(config.refName) match {
        case None =>
        case Some(list) =>
          for ((s, ref) <- list) {
            s ! config.getTargetOption(ref.targetName)
          }
      }

  }
}
