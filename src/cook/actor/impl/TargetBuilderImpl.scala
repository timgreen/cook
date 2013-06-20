package cook.actor.impl

import cook.actor.TargetBuilder
import cook.actor.impl.util.BatchResponser
import cook.actor.impl.util.TaskBuilder
import cook.app.Global
import cook.error.CookException
import cook.ref.TargetRef
import cook.target.Target
import cook.target.TargetAndResult
import cook.target.TargetResult
import cook.util.DagSolver

import akka.actor.TypedActor
import scala.collection.mutable
import scala.concurrent.{ Promise, Future, Await }
import scala.concurrent.duration._
import scala.util.{ Try, Success, Failure }


object TargetBuildTask extends TaskBuilder("TargetBuild")

class TargetCycleDepException(cycle: List[String])
  extends CookException("Found cycle dep in targets")


/**
 * step1: get target
 * step2: wait all dep target
 * step3: bulid self target
 */
class TargetBuilderImpl extends TargetBuilder with TypedActorBase {

  import ActorRefs._

  private val responser = new BatchResponser[String, TargetAndResult]()
  private val dagSolver = new DagSolver
  private val pendingTargets = mutable.Map[String, (Target[TargetResult], Future[List[Target[TargetResult]]])]()
  private def self = targetBuilder

  override def updateStatus {
    import cook.actor.TargetStatus

    val status = dagSolver.getStatus
    val done = status.done
    val x = status.processing + status.avaliable + status.pending
    val pending = pendingTargets.size
    val building = x - pending
    val unsolved = status.unsolved
    statusManager.updateTargetStatus(TargetStatus(done, building, pending, unsolved))
  }

  override def taskComplete(refName: String)(tryTargetAndResult: Try[TargetAndResult]) {
    log.debug("complete {} {}", refName, tryTargetAndResult)
    responser.complete(refName)(tryTargetAndResult)
    if (tryTargetAndResult.isSuccess) {
      dagSolver.markDone(refName)
      self.checkDag
    }
  }

  override def build(targetRef: TargetRef): Future[TargetAndResult] = {
    val refName = targetRef.refName
    log.debug("building {}", refName)
    responser.onTask(refName) {
      step1GetTarget(targetRef)
    }
  }

  private def step1GetTarget(targetRef: TargetRef) {
    import TypedActor.dispatcher
    targetManager.getTarget(targetRef) onComplete {
      case Success(t) =>
        self.step2WaitForDeps(t)
      case Failure(e) =>
        log.debug("step1GetTarget.failure {}", e)
        self.taskComplete(targetRef.refName)(Failure(e))
    }
  }

  override def step2WaitForDeps(target: Target[TargetResult]) {
    dagSolver.addDeps(target.refName, target.deps.map(_.refName)) match {
      case DagSolver.Ok =>
        import TypedActor.dispatcher
        val depTargets = Future.sequence(target.deps.toList map { d => self.build(d) map { _._1 } })
        pendingTargets(target.refName) = target -> depTargets
        depTargets onFailure {
          case e =>
            self.taskComplete(target.refName)(Failure(e))
        }
        self.checkDag
      case DagSolver.FoundDepCycle(cycle) =>
        self.taskComplete(target.refName)(Failure(new TargetCycleDepException(cycle)))
    }
  }

  override def checkDag {
    self.updateStatus
    if (dagSolver.hasAvaliable) {
      val avaliableTargetName = dagSolver.pop
      self.step3BuildTarget(avaliableTargetName)
      self.checkDag
    }
  }

  override def step3BuildTarget(targetName: String) {
    val optTarget = pendingTargets.remove(targetName)
    assert(optTarget.isDefined, "the target ready for build must in pending list: " + targetName)
    val (target, futureDepTargets) = optTarget.get
    self.updateStatus

    Global.workerDispatcher.execute(TargetBuildTask(targetName) {
      val depTargets = Await.result(futureDepTargets, 1 millis)
      self.taskComplete(targetName)(Try {
        target.build(depTargets)
        target -> target.result(depTargets)
      })
    })
  }
}
