package cook.actor.impl.util

class TaskBuilder(taskType: String) {

  import cook.util.LogSourceProvider._
  import akka.event.Logging

  private val log = Logging(cook.app.Global.system, this)

  def apply(taskName: String)(runBlock: => Unit): Runnable = new Runnable {
    override def run() {
      log.debug("Task {} '{}': Start", taskType, taskName)
      runBlock
      log.debug("Task {} '{}': End", taskType, taskName)
    }
  }
}
