package cook.actor

import cook.ref.TargetRef
import cook.target.Target
import cook.target.TargetAndResult
import cook.target.TargetResult

import scala.concurrent.{ Promise, Future, Await }
import scala.util.{ Try, Success, Failure }

trait TargetBuilder {

  def taskComplete(refName: String)(tryTargetResult: Try[TargetAndResult])
  def build(targetRef: TargetRef): Future[(TargetAndResult)]

  def step2WaitForDeps(target: Target[TargetResult])
  def step3BuildTarget(targetName: String)
  def checkDag
  def updateStatus
}
