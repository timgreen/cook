package cook.app

import akka.actor.ActorSystem

object Global {

  val system = ActorSystem("cook", Config.conf.getConfig("cook"))
  val workerDispatcher = system.dispatchers.lookup("worker-dispatcher")
}
