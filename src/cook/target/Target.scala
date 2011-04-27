package cook.target

import java.io.File
import java.util.Date

import scala.io.Source
import scala.collection.immutable.VectorBuilder
import scala.collection.mutable.HashSet

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

  def targetName(): String = {
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
          "One target should never been build twice: target \"%s\"".format(targetName))
    }
    println("Building target \"%s\"".format(targetName))
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

  lazy val allDepOutputDirs: HashSet[String] = {
    val set = new HashSet[String]
    for (d <- deps if (d.indexOf(":") != -1)) {
      val t = TargetManager.getTarget(new TargetLabel(path, d))
      set += t.outputDir.getAbsolutePath
      set ++= t.allDepOutputDirs
    }
    set
  }

  /**
   * Get dependence targets.
   *
   * Dependence come from "deps", "inputs", "tools"
   */
  lazy val depTargets: Seq[TargetLabel] = {
    val depsBuilder = new VectorBuilder[TargetLabel]
    for (
      i <- (deps ++ inputs ++ tools)
      if (i.indexOf(":") != -1)
    ) {
      depsBuilder += new TargetLabel(path, i)
    }
    depsBuilder.result
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
    if (!outputDir.isDirectory && !outputDir.mkdirs) {
      throw new TargetException(
          "Can not create output dir for target \"%s\": %s".format(name, outputDir.getPath))
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
    val splitArrayValueCmds = Seq[String](
      "OLD_IFS=\"$IFS\"",
      "IFS='|'",
      "INPUTS=( $INPUTS )",
      "DEP_OUTPUT_DIRS=( $DEP_OUTPUT_DIRS )",
      "ALL_DEP_OUTPUT_DIRS=( $ALL_DEP_OUTPUT_DIRS )",
      "TOOLS=( $TOOLS )",
      "IFS=\"$OLD_IFS\""
    )
    val pb = new ProcessBuilder(
        "/bin/bash", "-c",
        (splitArrayValueCmds ++ cmds).mkString(";"))
    pb.directory(outputDir)
    pb.redirectErrorStream(true)
    val env = pb.environment
    env.put("OUTPUT_DIR", outputDir.getAbsolutePath)
    env.put("NAME", name)
    // TODO(timgreen): figure out a better way to pass array values
    env.put("INPUTS", inputFiles.map(_.getAbsolutePath).mkString("|"))
    env.put("DEP_OUTPUT_DIRS", depOutputDirs.map(_.getAbsolutePath).mkString("|"))
    env.put("ALL_DEP_OUTPUT_DIRS", allDepOutputDirs.mkString("|"))
    env.put("TOOLS", toolFiles.map(_.getAbsolutePath).mkString("|"))

    val p = pb.start

    // Merge subprocess output
    // NOTE(timgreen): don't need p.waitFor here, will block on inputstream
    val is = p.getInputStream
    val bytes = Array[Byte](100)
    while (is.read(bytes) != -1) {
      System.out.write(bytes)
    }
    println("")

    if (p.exitValue != 0) {
      System.exit(p.exitValue)
    }
  }

  var isBuilded = false
}
