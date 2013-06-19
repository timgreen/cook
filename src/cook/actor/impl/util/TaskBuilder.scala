package cook.actor.impl.util

import cook.actor.StatusManager

class TaskBuilder(taskType: String) {

  import cook.util.LogSourceProvider._
  import akka.event.Logging

  private val log = Logging(cook.app.Global.system, this)

  def apply(taskName: String)
           (runBlock: => Unit)
           (implicit statusManager: StatusManager): Runnable = new Runnable {
    override def run() {
      log.debug("Task {} '{}': Start", taskType, taskName)
      statusManager.startTask(taskType, taskName)
      runBlock
      log.debug("Task {} '{}': End", taskType, taskName)
      statusManager.endTask(taskType, taskName)
    }
  }
}
