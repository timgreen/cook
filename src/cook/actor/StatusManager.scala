package cook.actor

trait StatusManager {

  def startTask(taskType: String, taskName: String)
  def endTask(taskType: String, taskName: String)
}
