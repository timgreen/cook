package cook.actor

import cook.ref.TargetRef
import cook.target.Target

import scala.concurrent.{ Promise, Future, Await }

trait TargetManager {

  def getTarget(targetRef: TargetRef): Future[Target[_]]
}
