package cook.actor.impl

import cook.actor.ConfigLoader
import cook.actor.LoadConfigClassTaskInfo
import cook.actor.impl.util.BatchResponser
import cook.app.Global
import cook.config.Config
import cook.config.ConfigEngine
import cook.config.ConfigRef
import cook.config.ConfigRefInclude
import cook.util.DagSolver

import akka.actor.TypedActor
import java.net.URLClassLoader
import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{ Try, Success, Failure }


object ConfigLoadTask {

  def apply(refName: String)(runBlock: => Unit): Runnable = new Runnable {
    override def run() {
      runBlock
    }
  }
}

/**
 *
 * step1: wait all dep configRef
 * step2: wait all dep config is already loaded
 * step3: load self config
 */
class ConfigLoaderImpl(rootIncludes: List[ConfigRefInclude]) extends ConfigLoader with TypedActorBase {

  import ActorRefs._

  private val responser = new BatchResponser[String, Config]()
  private val dagSolver = new DagSolver

  private def self = configLoader

  override def taskComplete(refName: String)(tryConfig: Try[Config]) {
    responser.complete(refName)(tryConfig)
    dagSolver.markDone(refName)
    self.checkDag
  }

  override def loadConfig(configRef: ConfigRef): Future[Config] = {
    val refName = configRef.refName

    responser.onTask(refName) {
      step1WaitDepConfigRefs(configRef)
    }
  }

  def step1WaitDepConfigRefs(configRef: ConfigRef) {

    // NOTE(timgreen): cycle check already been done on configRef level, so don't need check here.
    val depConfigFileRef =
      rootIncludes.map(_.ref) ++
      configRef.includes.map(_.ref)

    import TypedActor.dispatcher
    Future.traverse(depConfigFileRef.toList) { cookFileRef =>
      configRefManager.getConfigRef(cookFileRef)
    } onComplete self.step2WaitDepConfig(configRef)
  }

  override def step2WaitDepConfig(configRef: ConfigRef)(tryDepConfigRefs: Try[List[ConfigRef]]) {
    tryDepConfigRefs match {
      case Success(depConfigRefs) =>
        depUnsolvedTasks(configRef.refName) = LoadConfigClassTaskInfo(configRef, depConfigRefs)
        dagSolver.addDeps(configRef.refName, depConfigRefs.map(_.refName))
        depConfigRefs foreach { depConfigRef =>
          // TODO(timgreen): mark sure success
          self.loadConfig(depConfigRef)
        }
        self.checkDag
      case Failure(e) =>
        self.taskComplete(configRef.refName)(Failure(e))
    }
  }

  private val depUnsolvedTasks = mutable.Map[String, LoadConfigClassTaskInfo]()

  override def checkDag {
    if (dagSolver.hasAvaliable) {
      val refName = dagSolver.pop
      val Some(taskInfo) = depUnsolvedTasks.remove(refName)
      self.step3LoadConfigClass(taskInfo)
    }
  }

  override def step3LoadConfigClass(taskInfo: LoadConfigClassTaskInfo) {
    val refName = taskInfo.configRef.refName
    Global.workerDispatcher.execute(ConfigLoadTask(refName) {
      self.taskComplete(refName)(Try(doLoadConfig(taskInfo)))
    })
  }

  private def doLoadConfig(taskInfo: LoadConfigClassTaskInfo): Config = {
    ConfigEngine.load(taskInfo.configRef, rootIncludes, taskInfo.depConfigRefs)
  }
}
