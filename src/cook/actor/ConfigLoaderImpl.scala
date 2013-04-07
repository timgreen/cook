package cook.actor

import cook.actor.util.BatchResponser
import cook.app.Global
import cook.config.Config
import cook.config.ConfigRef
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
class ConfigLoaderImpl(val rootConfigRef: ConfigRef) extends ConfigLoader with TypedActorBase {

  private val responser = new BatchResponser[String, Config]()
  private val dagSolver = new DagSolver

  override def taskComplete(refName: String)(tryConfig: Try[Config]) {
    tryConfig match {
      case Success(config) =>
        responser.success(refName, config)
      case Failure(e) =>
        responser.failure(refName, e)
    }
  }

  override def loadConfig(configRef: ConfigRef): Future[Config] = {
    val refName = configRef.refName

    responser.onTask(refName) {
      step1WaitDepConfigRefs(configRef)
    }
  }

  def step1WaitDepConfigRefs(configRef: ConfigRef) {
    val self = TypedActor.self[ConfigLoader]

    // NOTE(timgreen): cycle check already been done on configRef level, so don't need check here.
    val depConfigFileRef = Set(
      ConfigRef.rootConfigFileRef
    ) ++ configRef.imports.map(_.ref) ++ rootConfigRef.mixins

    // TODO(timgreen): use another ec?
    import scala.concurrent.ExecutionContext.Implicits.global

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
          configLoader.loadConfig(depConfigRef)
        }
        checkDag
      case Failure(e) =>
        configLoader.taskComplete(configRef.refName)(Failure(e))
    }
  }

  private val depUnsolvedTasks = mutable.Map[String, LoadConfigClassTaskInfo]()
  private def checkDag = if (dagSolver.hasAvaliable) {
    val self = TypedActor.self[ConfigLoader]
    val refName = dagSolver.pop
    val Some(taskInfo) = depUnsolvedTasks.remove(refName)
    self.step3LoadConfigClass(taskInfo)
  }

  override def step3LoadConfigClass(taskInfo: LoadConfigClassTaskInfo) {
    val self = TypedActor.self[ConfigLoader]
    val refName = taskInfo.configRef.refName
    Global.workerDispatcher.execute(ConfigLoadTask(refName) {
      self.taskComplete(refName)(Try(doLoadConfig(taskInfo)))
    })
  }

  private def doLoadConfig(taskInfo: LoadConfigClassTaskInfo): Config = {
    // TODO(timgreen): gen source / compile

    val cl = getClassLoader(taskInfo)
    val clazz = cl.loadClass(taskInfo.configRef.configClassFullName)
    clazz.asInstanceOf[Class[Config]].newInstance
  }

  private def getClassLoader(taskInfo: LoadConfigClassTaskInfo) = {
    val cp = (taskInfo.configRef :: taskInfo.depConfigRefs).map(_.configByteCodeDir.toURI.toURL).toArray
    new URLClassLoader(cp, this.getClass.getClassLoader)
  }
}
