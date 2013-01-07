package cook.actor

import akka.actor.Actor
import akka.util.Timeout
import scala.concurrent.duration._


abstract class ActorBase extends Actor {

  // NOTE(timgreen): should not timeout here
  implicit val timeout = Timeout(100 days)
  implicit def executionContext = context.dispatcher
}
