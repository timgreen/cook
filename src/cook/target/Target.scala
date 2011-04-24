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
    val exeCmds: Seq[String],
    val isGenerateTarget: Boolean) {

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
    depOutputDirs = prepareDepOutputDirs(deps)
    toolFiles = prepareToolFiles(tools)
    // TODO(timgreen): check tools
    if (isBuilded) {
      throw new TargetException(
          "One target should never been build twice: target \"%s\"".format(name))
    }
    runCmds(cmds)
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
    if (isGenerateTarget) {
      FileUtil.getGenerateOutputDir(path, name)
    } else {
      FileUtil.getBuildOutputDir(path, name)
    }
  }

  def depTargets(): Seq[TargetLabel] = {
    deps.map {
      new TargetLabel(path, _)
    }
  }

  private[target]
  var inputFiles: Seq[File] = null
  var depOutputDirs: Seq[File] = null
  var toolFiles: Seq[File] = null

  def prepareInputFiles(inputs: Seq[String]) = labelToFiles(inputs)
  def prepareToolFiles(tools: Seq[String]) = labelToFiles(tools)

  /**
   * Get dep targets output dirs.
   */
  def prepareDepOutputDirs(deps: Seq[String]) : Seq[File] = {
    val depOutputDirsBuilder = new VectorBuilder[File]

    for (t <- deps) {
      val target = TargetManager.getTarget(new TargetLabel(path, t))
      depOutputDirsBuilder += target.outputDir
    }

    depOutputDirsBuilder.result
  }

  def mkOutputDir() {
    if (!outputDir.mkdirs) {
      throw new TargetException(
          "Can not create output dir for target \"%s\": %s".format(outputDir.getPath, name))
    }
  }

  /**
   * Convert Seq[label: String] to Seq[File]
   */
  def labelToFiles(labels: Seq[String]): Seq[File] = {
    val filesBuilder = new VectorBuilder[File]

    for (l <- labels) {
      if (l.indexOf(':') == -1) {
        filesBuilder += new FileLabel(path, l).file
      } else {
        val target = TargetManager.getTarget(new TargetLabel(path, l))
        filesBuilder ++= target.outputs
      }
    }

    val files = filesBuilder.result
    checkFiles(files)
    files
  }

  def checkFiles(files: Seq[File]) {
    for (f <- files) {
      if (!f.exists) {
        throw new TargetException("Required input file \"%s\" not exists".format(f.getPath))
      }
    }
  }

  def runCmds(cmds: Seq[String]) {
    mkOutputDir
    val pb = new ProcessBuilder(cmds: _*)
    pb.directory(outputDir)
    val env = pb.environment
    env.put("OUTPUT_DIR", outputDir.getAbsolutePath)
    env.put("INPUTS", BashArray(inputFiles.map(_.getAbsolutePath)))
    env.put("DEP_OUTPUT_DIRS", BashArray(depOutputDirs.map(_.getAbsolutePath)))
    env.put("TOOLS", BashArray(toolFiles.map(_.getAbsolutePath)))
    pb.start
  }

  var isBuilded = false
}

/**
 * TODO(timgreen): merge into shell util
 */
object BashArray {

  def apply(values: Seq[String]): String = {
    val v = values.map((s) => {
      "\"%s\"".format("[\"\\]".r.replaceAllIn(s, (m) => "\\" + m.toString))
    }).mkString(" ")
    "(%s)".format(v)
  }
}
