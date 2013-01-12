package cook.actor

import cook.actor.util.TokenBasedWorker
import cook.actor.util.WorkerTask
import cook.config.Config
import cook.config.ConfigRef
import cook.util.DagSolver

import akka.actor.ActorRef
import akka.pattern.ask
import akka.pattern.pipe
import java.net.URLClassLoader
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.control.Exception._

sealed trait ConfigLoaderWorkerTask extends WorkerTask
case class SolveDepConfigRefs(configRef: ConfigRef) extends ConfigLoaderWorkerTask
case class LoadConfigClassTask(
  configRef: ConfigRef,
  depConfigRefs: List[ConfigRef]
) extends ConfigLoaderWorkerTask
case class DoneSolveDepConfigRefs(configRef: ConfigRef, depConfigFileRefs: List[ConfigRef])
case class DoneLoadConfigClass(refName: String, config: Config)

class ConfigLoaderActor extends ActorBase with TokenBasedWorker[ConfigLoaderWorkerTask] {

  private val waiters = mutable.Map[String, mutable.ListBuffer[ActorRef]]()
  private val doneSet = mutable.Set[String]()
  private val dagSolver = new DagSolver

  val configRefManagerActor = context.actorFor("./ConfigRefManager")
  override val workerTokenManagerActor = context.actorFor("./WorkerTokenManager")

  private var rootConfigRef: ConfigRef = _

  def receive = workerReceive orElse {
    case LoadConfig(configRef) =>
      if (!doneSet.contains(configRef.refName)) {
        val list = waiters.getOrElseUpdate(
          configRef.fileRef.refName, mutable.ListBuffer[ActorRef]())
        list += sender
        if (list.size == 1) {
          self ! SolveDepConfigRefs(configRef)
        }
      }
    case DoneSolveDepConfigRefs(configRef, depConfigRefs) =>
      depUnsolvedTasks(configRef.refName) = LoadConfigClassTask(configRef, depConfigRefs)
      dagSolver.addDeps(configRef.refName, depConfigRefs.map(_.refName))
      depConfigRefs foreach { depConfigRef =>
        self ! LoadConfig(depConfigRef)
      }
      checkDag
    case DoneLoadConfigClass(refName, config) =>
      dagSolver.markDone(refName)
      checkDag
      waiters.remove(refName) match {
        case None =>
        case Some(list) =>
          for (s <- list) {
            s ! ConfigLoaded(refName, config)
          }
      }
      doneSet += refName
    case PreFetchRootConfigRef =>
      val ref = ConfigRef.rootConfigFileRef
      val f = configRefManagerActor ask LoadConfigRef(ref.refName, ref)
      rootConfigRef = Await.result(f, 100 days).asInstanceOf[FindConfigRef].configRef
  }

  private val depUnsolvedTasks = mutable.Map[String, LoadConfigClassTask]()
  private def checkDag = if (dagSolver.hasAvaliable) {
    val refName = dagSolver.pop
    val Some(task) = depUnsolvedTasks.remove(refName)
    self ! task
  }

  override def doRunWorkerTask(token: Int, task: ConfigLoaderWorkerTask): Future[Unit] = {
    task match {
      case SolveDepConfigRefs(configRef) =>
        loadDepConfigDefs(configRef) map { depConfigRefs =>
          self ! DoneSolveDepConfigRefs(configRef, depConfigRefs)
        }
      case LoadConfigClassTask(configRef, depConfigRefs) =>
        Future {
          load(configRef, depConfigRefs) match {
            case Some(config) =>
              self ! DoneLoadConfigClass(config.refName, config)
            case None =>
              // TODO(timgreen): error report
          }
        }
    }
  }

  private def load(configRef: ConfigRef, depConfigRefs: List[ConfigRef]): Option[Config] = {
      // TODO(timgreen): gen source / compile

      val cl = getClassLoader(configRef, depConfigRefs)
      allCatch.opt {
        val clazz = cl.loadClass(configRef.configClassFullName)
        clazz.asInstanceOf[Class[Config]].newInstance
      }
  }

  private def loadDepConfigDefs(configRef: ConfigRef): Future[List[ConfigRef]] = {
    // NOTE(timgreen): cycle check already been done on configRef level, so don't need check here.
    val depConfigFileRef = Set(
      ConfigRef.rootConfigFileRef
    ) ++ configRef.imports.map(_.ref) ++ rootConfigRef.mixins

    val list = depConfigFileRef.toList

    Future.traverse(list) { configFileRef =>
      val msg = LoadConfigRef(configFileRef.refName, configFileRef)
      ask(configRefManagerActor, msg).mapTo[FindConfigRef].map(_.configRef)
    }
  }

  private def getClassLoader(configRef: ConfigRef, depConfigRefs: List[ConfigRef]) = {
    val cp = (configRef :: depConfigRefs).map(_.configByteCodeDir.toURI.toURL).toArray
    new URLClassLoader(cp, this.getClass.getClassLoader)
  }
}
