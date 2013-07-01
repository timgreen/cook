package cook.actor

case class TargetStatus(done: Int, cached: Int, building: Int, pending: Int, unsolved: Int)

trait StatusManager {

  def startTask(taskType: TaskType.Value, taskName: String)
  def endTask(taskType: TaskType.Value, taskName: String)
  def updateTargetStatus(targetStatus: TargetStatus)
  // NOTE(timgreen): return value Int is used to mark this request blocking.
  def blockToFinish: Int
}
