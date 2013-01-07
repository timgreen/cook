package cook.actor

import cook.actor.util.TokenBasedWorker
import cook.actor.util.WorkerTask
import cook.config.Config
import cook.config.ConfigRef

import akka.actor.ActorRef
import akka.pattern.ask
import akka.pattern.pipe
import java.net.URLClassLoader
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.control.Exception._

case class ConfigLoadTaskStatus(
  depConfigRefs: List[ConfigRef],
  unloaded: List[ConfigRef],
  shouldLoadDeps: Boolean = true
)
case class ConfigLoadTask(
  configRef: ConfigRef,
  statusOpt: Option[ConfigLoadTaskStatus]
) extends WorkerTask

class ConfigLoaderActor extends ActorBase with TokenBasedWorker[ConfigLoadTask] {

  private val waiters = mutable.Map[String, mutable.ListBuffer[ActorRef]]()

  val configRefManagerActor = context.actorFor("./ConfigRefManager")
  override val workerTokenManagerActor = context.actorFor("./WorkerTokenManager")

  private val loadedConfigRefNames = mutable.Set[String]()
  private var rootConfigRef: ConfigRef = _

  def receive = workerReceive orElse {
    case LoadConfig(configRef) =>
      val list = waiters.getOrElseUpdate(
        configRef.fileRef.refName, mutable.ListBuffer[ActorRef]())
      list += sender
      if (list.size == 1) {
        self ! ConfigLoadTask(configRef, None)
      }
    case LoadConfigSuccess(refName, config) =>
      loadedConfigRefNames += refName
      waiters.remove(refName) match {
        case None =>
        case Some(list) =>
          for (s <- list) {
            s ! ConfigLoaded(refName, config)
          }
      }
    case PreFetchRootConfigRef =>
      val ref = ConfigRef.rootConfigFileRef
      val f = configRefManagerActor ask LoadConfigRef(ref.refName, ref)
      rootConfigRef = Await.result(f, 100 days).asInstanceOf[FindConfigRef].configRef
  }

  private def isNotLoaded(refName: String) = !loadedConfigRefNames.contains(refName)

  override def doRunWorkerTask(token: Int, task: ConfigLoadTask): Future[Unit] = {
    val f = task.statusOpt match {
      case Some(status) =>
        val unloaded = status.unloaded filter { x => isNotLoaded(x.fileRef.refName) }
        if (status.shouldLoadDeps) {
          unloaded foreach { depConfigRef =>
            self ! LoadConfig(depConfigRef)
          }
        }
        Future(status.copy(unloaded = unloaded, shouldLoadDeps = false))
      case None =>
        loadDepConfigDefs(task.configRef)
    }

    f map { status =>
      if (status.unloaded.isEmpty) {
        load(task.configRef, status.depConfigRefs) match {
          case Some(config) =>
            self ! LoadConfigSuccess(config.refName, config)
          case None =>
            // TODO(timgreen): error report
        }
      } else {
        // deps is not ready yet, push back task
        self ! ConfigLoadTask(task.configRef, Some(status))
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

  private def loadDepConfigDefs(configRef: ConfigRef): Future[ConfigLoadTaskStatus] = {
    // NOTE(timgreen): cycle check already been done on configRef level, so don't need check here.
    val depConfigFileRef = Set(
      ConfigRef.rootConfigFileRef
    ) ++ configRef.imports.map(_.ref) ++ rootConfigRef.mixins

    val list = depConfigFileRef.toList

    Future.traverse(list) { configFileRef =>
      val msg = LoadConfigRef(configFileRef.refName, configFileRef)
      ask(configRefManagerActor, msg).mapTo[FindConfigRef].map(_.configRef)
    } map { depConfigRefs =>
      ConfigLoadTaskStatus(depConfigRefs, depConfigRefs)
    }
  }

  private def getClassLoader(configRef: ConfigRef, depConfigRefs: List[ConfigRef]) = {
    val cp = (configRef :: depConfigRefs).map(_.configByteCodeDir.toURI.toURL).toArray
    new URLClassLoader(cp, this.getClass.getClassLoader)
  }
}
