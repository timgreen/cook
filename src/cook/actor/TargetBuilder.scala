package cook.actor

import cook.ref.TargetRef
import cook.target.Target
import cook.target.TargetResult

import scala.concurrent.{ Promise, Future, Await }
import scala.util.{ Try, Success, Failure }

trait TargetBuilder {

  def taskComplete(refName: String)(tryTargetResult: Try[TargetResult])
  def build(targetRef: TargetRef): Future[TargetResult]

  def step2WaitForDeps(target: Target[TargetResult])
  def step3BuildTarget(targetName: String)
  def checkDag
}
