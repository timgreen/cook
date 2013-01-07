package cook.actor

import akka.actor.ActorSystem
import akka.actor.Props

object Actors {

  val system = ActorSystem("cook")

  val configRefManagerActor = system.actorOf(Props[ConfigRefManagerActor], name = "ConfigRefManager")
  val configLoaderActor = system.actorOf(Props[ConfigLoaderActor], name = "ConfigLoaderActor")
  configLoaderActor ! PreFetchRootConfigRef
  val configManagerActor = system.actorOf(Props[ConfigManagerActor], name = "ConfigManager")
  val targetManagerActor = system.actorOf(Props[TargetManagerActor], name = "TargetManager")


}
