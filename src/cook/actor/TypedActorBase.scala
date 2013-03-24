package cook.actor

import akka.actor.{ TypedActor, TypedProps }

trait TypedActorBase {

  protected def configRefLoader =
    TypedActor(TypedActor.context.system).typedActorOf(
      TypedProps[ConfigRefLoader],
      TypedActor.context.system.actorFor("ConfigRefLoader"))
}
