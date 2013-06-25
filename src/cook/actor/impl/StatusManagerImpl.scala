package cook.actor.impl

import cook.actor.StatusManager
import cook.actor.TargetStatus

import scala.collection.mutable

class StatusManagerImpl extends StatusManager {

  import ActorRefs._

  private val runningTasks = mutable.Set[(String, String)]()
  private var _targetStatus = TargetStatus(0, 0, 0, 0, 1)

  private def isValidType(taskType: String): Boolean = {
    // TODO(timgreen):
    true
  }

  private def fireUpdate {
    // TODO(timgreen):
    consoleOutputter.update(_targetStatus, runningTasks.toSet)
  }

  override def updateTargetStatus(targetStatus: TargetStatus) {
    _targetStatus = targetStatus
    fireUpdate
  }

  override def startTask(taskType: String, taskName: String) {
    assert(isValidType(taskType), "Invalid task type: " + taskType + " " + taskName)
    assert(!runningTasks.contains(taskType -> taskName), "duplicated task: " + taskType + " " + taskName)
    runningTasks += (taskType -> taskName)
    fireUpdate
  }

  override def endTask(taskType: String, taskName: String) {
    assert(isValidType(taskType), "Invalid task type: " + taskType + " " + taskName)
    assert(runningTasks.contains(taskType -> taskName), "can not end an unexisted task: " + taskType + " " + taskName)
    runningTasks -= (taskType -> taskName)
    fireUpdate
  }

}
