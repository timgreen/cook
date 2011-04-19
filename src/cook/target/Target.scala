package cook.target

import java.io.File
import java.util.Date

import scala.collection.mutable.HashMap

import cook.util.FileUtil

class Target(
    val name: String,
    val basePath: String,
    val cmds: Seq[String],
    val inputs: Seq[File],
    val outputs: Seq[File],
    val deps: Seq[String],
    val exeCmds: Seq[String]) {

  /**
   * Only executeable target can be run by "cook run"
   */
  def isExecutable = (exeCmds != null)

  /**
   * Target it self doesn't care about deps, it only care about the input and output,
   * deps will be done by Target Manager.
   */
  def run() {
    // TODO(timgreen): add cache detect
    //
    // 0. save current timestamp
    // 1. check whether all inputs is ready
    // 2. run cmd
    // 3. check whether all output is generated

    val currentTimestamp = new Date().getTime
    checkIntputs()
    runCmd()
    checkOutputs()
  }

  private[target]
  def checkIntputs() {
    checkFiles(inputs)
  }

  def checkOutputs() {
    checkFiles(outputs)
  }

  def checkFiles(files: Seq[File]) {
    for (f <- inputs) {
      if (!f.exists) {
        throw new TargetException("Required input file \"%s\" not exists".format(f.getPath))
      }
    }
  }

  def runCmd() {
    run(cmds)
  }

  def run(cmds: Seq[String]) {
    val pb = new ProcessBuilder(cmds: _*)
    pb.directory(FileUtil(basePath))
    pb.start
  }
}

object Targets {

  val targets = new HashMap[String, Target]

  def push(t: Target) {
    targets.put(t.name, t)
  }

  def hasTarget(name: String) = targets.contains(name)

}

class TargetException(error: String) extends RuntimeException
