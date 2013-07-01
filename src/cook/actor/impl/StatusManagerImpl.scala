package cook.actor.impl

import cook.actor.StatusManager
import cook.actor.TargetStatus
import cook.actor.TaskType

import scala.collection.mutable

class StatusManagerImpl extends StatusManager with TypedActorBase {

  import ActorRefs._

  private val runningTasks = mutable.Set[(TaskType.Value, String)]()
  private var _targetStatus = TargetStatus(0, 0, 0, 0, 1)

  private def fireUpdate {
    // TODO(timgreen):
    consoleOutputter.update(_targetStatus, runningTasks.toSet)
  }

  override def updateTargetStatus(targetStatus: TargetStatus) {
    _targetStatus = targetStatus
    fireUpdate
  }

  override def startTask(taskType: TaskType.Value, taskName: String) {
    assert(!runningTasks.contains(taskType -> taskName), "duplicated task: " + taskType + " " + taskName)
    runningTasks += (taskType -> taskName)
    fireUpdate
  }

  override def endTask(taskType: TaskType.Value, taskName: String) {
    assert(runningTasks.contains(taskType -> taskName), "can not end an unexisted task: " + taskType + " " + taskName)
    runningTasks -= (taskType -> taskName)
    fireUpdate
  }

  override def blockToFinish: Int = 0
}
