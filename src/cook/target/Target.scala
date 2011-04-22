package cook.target

import java.io.File
import java.util.Date

import org.apache.tools.ant.DirectoryScanner

import cook.util._

class Target(
    val path: String,
    val name: String,
    val cmds: Seq[String],
    val inputs: Seq[File],
    val deps: Seq[String],
    val exeCmds: Seq[String]) {

  def fullname(): String = {
    path + ":" + name
  }

  /**
   * Only executeable target can be run by "cook run"
   */
  def isExecutable = (exeCmds != null)

  /**
   * Target it self doesn't care about deps, it only care about the input and output,
   * deps will be done by Target Manager.
   */
  def build() {
    // TODO(timgreen): add cache detect
    //
    // 0. save current timestamp
    // 1. check whether all inputs is ready
    // 2. run cmd
    // 3. check whether all output is generated

    val currentTimestamp = new Date().getTime
    checkIntputs()
    if (isBuilded) {
      throw new TargetException(
          "One target should never been build twice: target \"%s\"".format(name))
    }
    run(cmds)
    isBuilded = true
  }

  def outputs(): Seq[String] = {
    if (!isBuilded) {
      throw new TargetException(
          "Outputs will become available after build: target \"%s\"".format(name))
    }

    val ds = new DirectoryScanner
    ds.setBasedir(outputDir)
    ds.scan
    ds.getIncludedFiles.toSeq
  }

  def outputDir(): File = {
    FileUtil("%s/%s/%s%s".format(Target.COOK_BUILD, path, Target.OUTPUT_PREFIX, name))
  }

  def depTargets(): Seq[TargetLabel] = {
    deps.map {
      new TargetLabel(path, _)
    }
  }

  private[target]
  def checkIntputs() {
    checkFiles(inputs)
  }

  def mkOutputDir() {
    if (!outputDir.mkdirs) {
      throw new TargetException(
          "Can not create output dir for target \"%s\": %s".format(outputDir.getPath, name))
    }
  }

  def checkFiles(files: Seq[File]) {
    for (f <- inputs) {
      if (!f.exists) {
        throw new TargetException("Required input file \"%s\" not exists".format(f.getPath))
      }
    }
  }

  def run(cmds: Seq[String]) {
    mkOutputDir
    val pb = new ProcessBuilder(cmds: _*)
    pb.directory(outputDir)
    val env = pb.environment
    env.put("OUTPUT_DIR", outputDir.getAbsolutePath)
    env.put("INPUTS", inputs.map(_.getAbsolutePath).mkString(" "))
    pb.start
  }

  var isBuilded = false
}

object Target {

  val OUTPUT_PREFIX = "COOK_TARGET_"
  val COOK_GEN = "cook_gen"
  val COOK_BUILD = "cook_build"
}
