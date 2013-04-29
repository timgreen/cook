package cook.actor

import cook.actor.impl._
import cook.app.Global
import cook.config.ConfigRef

import akka.actor.{ ActorContext, TypedActor, TypedProps }

object Actors {

  val system = Global.system

  val configRefLoader =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigRefLoader],
        new ConfigRefLoaderImpl),
      "ConfigRefLoader")

  val configRefVerifier =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigRefVerifier],
        new ConfigRefVerifierImpl),
      "ConfigRefVerifier")

  val configRefManager =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigRefManager],
        new ConfigRefManagerImpl),
      "ConfigRefManager")

  val rootConfigRef = new ConfigRef(ConfigRef.rootConfigFileRef)
  val configLoader =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigLoader],
        new ConfigLoaderImpl(rootConfigRef)),
      "ConfigLoader")

  val configManager =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigManager],
        new ConfigManagerImpl),
      "ConfigManager")

  val targetManager =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[TargetManager],
        new TargetManagerImpl),
      "TargetManager")

  val targetBuilder =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[TargetBuilder],
        new TargetBuilderImpl),
      "TargetBuilder")
}
