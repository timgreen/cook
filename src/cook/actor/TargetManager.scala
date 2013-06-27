package cook.actor

import cook.ref.TargetRef
import cook.target.Target
import cook.target.TargetResult

import scala.concurrent.{ Promise, Future, Await }
import scala.util.Try

trait TargetManager {

  def nativeTaskComplete(refName: String)(tryTarget: Try[Target[TargetResult]])
  def getTarget(targetRef: TargetRef): Future[Target[TargetResult]]
}
