package cook.actor

case class TargetStatus(done: Int, cached: Int, building: Int, pending: Int, unsolved: Int)

trait StatusManager {

  def startTask(taskType: String, taskName: String)
  def endTask(taskType: String, taskName: String)
  def updateTargetStatus(targetStatus: TargetStatus)
}
