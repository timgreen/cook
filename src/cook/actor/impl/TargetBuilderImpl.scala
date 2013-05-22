package cook.actor.impl

import cook.actor.TargetBuilder
import cook.actor.impl.util.BatchResponser
import cook.ref.TargetRef
import cook.target.Target
import cook.target.TargetResult
import cook.util.DagSolver

import akka.actor.TypedActor
import scala.collection.mutable
import scala.concurrent.{ Promise, Future, Await }
import scala.util.{ Try, Success, Failure }


/**
 * step1: get target
 * step2: wait all dep target
 * step3: bulid self target
 */
class TargetBuilderImpl extends TargetBuilder with TypedActorBase {

  private val responser = new BatchResponser[String, TargetResult]()
  private val dagSolver = new DagSolver
  private val builtTargetNames = mutable.Set[String]()
  private val pendingTargets = mutable.Map[String, Target[_]]()
  private def self = targetBuilder

  override def taskComplete(refName: String)(tryTargetResult: Try[TargetResult]) {
    responser.complete(refName)(tryTargetResult)
    dagSolver.markDone(refName)
    self.checkDag
  }

  override def build(targetRef: TargetRef): Future[TargetResult] = {
    val refName = targetRef.refName
    responser.onTask(refName) {
      step1GetTarget(targetRef)
    }
  }

  private def step1GetTarget(targetRef: TargetRef) {
    import TypedActor.dispatcher
    targetManager.getTarget(targetRef) map { t =>
      self.step2WaitForDeps(t)
    }
  }

  override def step2WaitForDeps(target: Target[TargetResult]) {
    if (builtTargetNames.contains(target.refName)) {
      self.taskComplete(target.refName)(Try(target.result))
    } else {
      pendingTargets(target.refName) = target
      dagSolver.addDeps(target.refName, target.deps.map(_.refName))
      target.deps foreach self.build
      self.checkDag
    }
  }

  override def step3BuildTarget(targetName: String) {
    val target = pendingTargets.remove(targetName)
    // TODO(timgreen):
    // target.build
    // ...
    // self.onComplete(targetName)(...)
    // builtTargetNames += targetName
  }

  override def checkDag {
    if (dagSolver.hasAvaliable) {
      val avaliableTargetName = dagSolver.pop
      self.step3BuildTarget(avaliableTargetName)
    }
  }
}