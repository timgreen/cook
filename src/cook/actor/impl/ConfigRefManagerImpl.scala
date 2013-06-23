package cook.actor.impl

import cook.actor.ConfigRefManager
import cook.actor.impl.util.BatchResponser
import cook.actor.impl.util.TaskBuilder
import cook.app.Global
import cook.config.ConfigRef
import cook.console.ops._
import cook.error._
import cook.ref.FileRef
import cook.util.DagSolver

import scala.concurrent.{ Future, Promise, Await }
import scala.concurrent.duration._
import scala.collection.mutable
import scala.util.{ Try, Success, Failure }
import akka.actor.{ TypedActor, TypedProps }

class ConfigCycleIncludeException(consoleOps: ConsoleOps) extends CookException(consoleOps, null)

class ConfigRefManagerImpl extends ConfigRefManager with TypedActorBase {

  import ActorRefs._

  private val responser = new BatchResponser[String, ConfigRef]()
  private val dagSolver = new DagSolver
  private val pendingRefs = mutable.Map[String, ConfigRef]()

  private def self = configRefManager

  override def getConfigRef(cookFileRef: FileRef): Future[ConfigRef] = {
    val refName = cookFileRef.refName
    log.debug("getConfigRef {}", refName)
    responser.onTask(refName) {
      step1GetConfigRef(refName, cookFileRef)
    }
  }

  override def taskComplete(refName: String)(tryConfigRef: Try[ConfigRef]) {
    responser.complete(refName)(tryConfigRef)
    if (tryConfigRef.isSuccess) {
      dagSolver.markDone(refName)
    }
  }

  private def step1GetConfigRef(refName: String, cookFileRef: FileRef) {
    log.debug("doGetConfigRef {} {}", refName, cookFileRef)
    // TODO(timgreen): use another ec?
    import TypedActor.dispatcher
    configRefLoader.loadConfigRef(cookFileRef) onComplete {
      case Success(configRef) =>
        self.step2LoadIncludeRefs(refName, configRef)
      case Failure(e) =>
        self.taskComplete(refName)(Failure(e))
    }
  }

  override def step2LoadIncludeRefs(refName: String, configRef: ConfigRef) {
    // TODO(timgreen): use another ec?
    import TypedActor.dispatcher

    dagSolver.addDeps(refName, configRef.includes.map(_.ref.refName) toList) match {
      case DagSolver.Ok =>
        Future.sequence(configRef.includes map { i => self.getConfigRef(i.ref) }) onFailure {
          case e =>
            self.taskComplete(refName)(Failure(e))
        }
        pendingRefs(refName) = configRef
        self.checkDag
      case DagSolver.FoundDepCycle(cycle) =>
        self.taskComplete(refName)(Failure(configCycleIncludeException(cycle)))
    }
  }

  override def checkDag {
    if (dagSolver.hasAvaliable) {
      val avaliableRefName = dagSolver.pop
      val optRef = pendingRefs.remove(avaliableRefName)
      assert(optRef.isDefined, "avaliable ref must be pending: " + avaliableRefName)
      self.taskComplete(avaliableRefName)(Success(optRef.get))
      self.checkDag
    }
  }

  private def configCycleIncludeException(cycle: List[String]) = {
    val header = "found cycle include in config:" :: newLine
    val ops = cycle map { c =>
      indent :: strong(c) :: newLine
    }
    new ConfigCycleIncludeException(header :: ops ::: indent :: strong(cycle.head))
  }
}
