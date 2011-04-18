package cook.target

import scala.collection.mutable.HashMap

class Target(
    val name: String,
    val basePath: String,
    val input: Seq[String],
    val output: Seq[String],
    val deps: Seq[String],
    val exeCmd: String) {

  /**
   * Only executeable target can be run by "cook run"
   */
  def isExecutable = (exeCmd != null)

  // TODO(timgreen): impl Target
}

object Targets {

  val targets = new HashMap[String, Target]

  def push(t: Target) {
    targets.put(t.name, t)
  }
}
