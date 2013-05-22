package cook.actor

import cook.actor.impl._
import cook.app.Global
import cook.config.ConfigRef

import akka.actor.{ ActorContext, TypedActor, TypedProps }
import akka.util.Timeout
import scala.concurrent.duration._

object Actors {

  val system = Global.system
  // NOTE(timgreen): there is no inf const for Timeout.
  val timeout = Timeout(100 days)

  val configRefLoader =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigRefLoader],
        new ConfigRefLoaderImpl).withTimeout(timeout),
      "ConfigRefLoader")

  val configRefVerifier =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigRefVerifier],
        new ConfigRefVerifierImpl).withTimeout(timeout),
      "ConfigRefVerifier")

  val configRefManager =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigRefManager],
        new ConfigRefManagerImpl).withTimeout(timeout),
      "ConfigRefManager")

  val rootConfigRef = new ConfigRef(ConfigRef.rootConfigFileRef)
  val configLoader =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigLoader],
        new ConfigLoaderImpl(rootConfigRef)).withTimeout(timeout),
      "ConfigLoader")

  val configManager =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigManager],
        new ConfigManagerImpl).withTimeout(timeout),
      "ConfigManager")

  val targetManager =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[TargetManager],
        new TargetManagerImpl).withTimeout(timeout),
      "TargetManager")

  val targetBuilder =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[TargetBuilder],
        new TargetBuilderImpl).withTimeout(timeout),
      "TargetBuilder")
}
