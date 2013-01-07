package cook.actor.util

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable
import scala.concurrent.Future

trait WorkerTask

trait TokenBasedWorker[Task <: WorkerTask] { this: Actor =>

  protected val workerTokenManagerActor: ActorRef
  private val pendingTasks = mutable.Queue[Task]()

  def workerReceive: PartialFunction[Any, Unit] = {
    case AssignWorkerToken(token) =>
      if (pendingTasks.isEmpty) {
        workerTokenManagerActor ! ReturnWorkerToken(token)
      } else {
        runWorkerTask(token, pendingTasks.dequeue)
      }
    case task: Task =>
      // add task
      pendingTasks enqueue task
      askWorkerToken
  }

  def askWorkerToken = {
    if (pendingTasks.nonEmpty) {
      workerTokenManagerActor ! AskWorkerToken
    }
  }

  final def runWorkerTask(token: Int, task: Task) {
    implicit val executionContext = context.dispatcher
    doRunWorkerTask(token, task) onComplete {
      case _ =>
        workerTokenManagerActor ! ReturnWorkerToken(token)
        askWorkerToken
    }
  }
  def doRunWorkerTask(token: Int, task: Task): Future[Unit]
}
