package cook.target

import java.io.File
import java.util.Date

import scala.collection.immutable.VectorBuilder

import org.apache.tools.ant.DirectoryScanner

import cook.util._

class Target(
    val path: String,
    val name: String,
    val cmds: Seq[String],
    val inputs: Seq[String],
    val deps: Seq[String],
    val tools: Seq[String],
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
    inputFiles = prepareInputFiles(inputs)
    // TODO(timgreen): check tools
    if (isBuilded) {
      throw new TargetException(
          "One target should never been build twice: target \"%s\"".format(name))
    }
    run(cmds)
    isBuilded = true
  }

  def outputs(): Seq[File] = {
    if (!isBuilded) {
      throw new TargetException(
          "Outputs will become available after build: target \"%s\"".format(name))
    }

    val ds = new DirectoryScanner
    ds.setBasedir(outputDir)
    ds.scan
    ds.getIncludedFiles.map {
      new File(outputDir, _)
    }
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
  var inputFiles: Seq[File] = null

  /**
   * Convert Seq[String] to Seq[File]
   */
  def prepareInputFiles(inputs: Seq[String]): Seq[File] = {
    val inputFilesBuilder = new VectorBuilder[File]

    for (i <- inputs) yield {
      if (i.indexOf(':') == -1) {
        inputFilesBuilder += new FileLabel(path, i).file
      } else {
        val target = TargetManager.getTarget(new TargetLabel(path, i))
        inputFilesBuilder ++= target.outputs
      }
    }

    checkFiles(inputFiles)

    inputFilesBuilder.result
  }

  def mkOutputDir() {
    if (!outputDir.mkdirs) {
      throw new TargetException(
          "Can not create output dir for target \"%s\": %s".format(outputDir.getPath, name))
    }
  }

  def checkFiles(files: Seq[File]) {
    for (f <- files) {
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
    env.put("INPUTS", inputFiles.map(_.getAbsolutePath).mkString(" "))
    pb.start
  }

  var isBuilded = false
}

object Target {

  val OUTPUT_PREFIX = "COOK_TARGET_"
  val COOK_GEN = "cook_gen"
  val COOK_BUILD = "cook_build"
}
