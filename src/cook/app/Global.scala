package cook.app

import akka.actor.ActorSystem

object Global {

  val system = ActorSystem()
  val workerDispatcher = system.dispatchers.lookup("worker-dispatcher")
}
