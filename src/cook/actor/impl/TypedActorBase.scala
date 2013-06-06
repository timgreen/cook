package cook.actor.impl

import cook.actor.ConfigLoader
import cook.actor.ConfigManager
import cook.actor.ConfigRefLoader
import cook.actor.ConfigRefManager
import cook.actor.ConfigRefVerifier
import cook.actor.TargetBuilder
import cook.actor.TargetManager
import cook.app.Global

import akka.actor.{ TypedActor, TypedProps }
import akka.event.Logging

trait TypedActorBase {
  val log = Logging(TypedActor.context.system, TypedActor.context.self)
}

object ActorRefs {

  import Global.system

  lazy val configRefLoader =
    TypedActor(system).typedActorOf(
      TypedProps[ConfigRefLoader],
      system.actorFor("/user/ConfigRefLoader"))

  lazy val configRefVerifier =
    TypedActor(system).typedActorOf(
      TypedProps[ConfigRefVerifier],
      system.actorFor("/user/ConfigRefVerifier"))

  lazy val configRefManager =
    TypedActor(system).typedActorOf(
      TypedProps[ConfigRefManager],
      system.actorFor("/user/ConfigRefManager"))

  lazy val configLoader =
    TypedActor(system).typedActorOf(
      TypedProps[ConfigLoader],
      system.actorFor("/user/ConfigLoader"))

  lazy val configManager =
    TypedActor(system).typedActorOf(
      TypedProps[ConfigManager],
      system.actorFor("/user/ConfigManager"))

  lazy val targetManager =
    TypedActor(system).typedActorOf(
      TypedProps[TargetManager],
      system.actorFor("/user/TargetManager"))

  lazy val targetBuilder =
    TypedActor(system).typedActorOf(
      TypedProps[TargetBuilder],
      system.actorFor("/user/TargetBuilder"))
}
