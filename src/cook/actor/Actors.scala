package cook.actor

import cook.actor.impl._
import cook.app.Config
import cook.app.Global
import cook.config.ConfigRef

import akka.actor.{ ActorContext, TypedActor, TypedProps }
import akka.util.Timeout
import scala.concurrent.duration._

object Actors {

  import Global.system

  val configRefLoader =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigRefLoader],
        new ConfigRefLoaderImpl),
      "ConfigRefLoader")

  val configRefManager =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigRefManager],
        new ConfigRefManagerImpl),
      "ConfigRefManager")

  val configLoader =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConfigLoader],
        new ConfigLoaderImpl(Config.rootIncludes)),
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

  val consoleOutputter =
    TypedActor(system).typedActorOf(
      TypedProps(
        classOf[ConsoleOutputter],
        new ConsoleOutputterImpl),
      "ConsoleOutputter")
}
