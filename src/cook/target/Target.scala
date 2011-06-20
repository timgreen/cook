package cook.target

import java.io.File
import java.io.PrintStream
import java.util.Date

import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.immutable.VectorBuilder
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.io.Source

import org.apache.tools.ant.DirectoryScanner

import cook.app.console.CookConsole
import cook.config.runner.value._
import cook.util._

class Target(
    val path: String,
    val name: String,
    val outputType: String,
    val cmds: Seq[String],
    val inputs: Seq[FileLabel],
    val deps: Seq[TargetLabel],
    val exeCmds: Seq[String],
    val preBuild: FunctionValue,
    val postBuild: FunctionValue,
    val preRun: FunctionValue) {

  var values = new HashMap[String, Value]

  def targetName(): String = {
    path + ":" + name
  }

  /**
   * Only executeable target can be run by "cook run"
   */
  def isExecutable = exeCmds.nonEmpty

  def execute() {
    if (!isExecutable) {
      throw new TargetException("Target \"%s\" is not executeable", targetName)
    }

    runCmds(exeCmds, runLogFile, runShFile, true)
  }

  /**
   * Target it self doesn't care about deps, it only care about the input and output,
   * deps will be done by Target Manager.
   */
  def build() {
    // 0. save current timestamp
    // 1. check whether all inputs is ready
    // 2. run cmd
    // 3. Save cache data

    val currentTimestamp = new Date().getTime
    inputFiles = prepareInputFiles(inputs)
    depOutputDirs = prepareDepOutputDirs(deps)
    if (isBuilded) {
      throw new TargetException(
          "One target should never been build twice: target \"%s\"",
          targetName)
    }

    if (!isCached) {
      runCmds(cmds, buildLogFile, buildShFile, false)
    }
    isBuilded = true
    saveCacheMeta
  }

  def outputs(): Seq[File] = {
    if (!isBuilded) {
      throw new TargetException("Outputs will become available after build: target \"%s\"", name)
    }

    val ds = new DirectoryScanner
    ds.setBasedir(outputDir)
    ds.scan
    ds.getIncludedFiles.map {
      new File(outputDir, _)
    }
  }

  def outputDir(): File = {
    FileUtil.getBuildOutputDir(path, name)
  }

  lazy val allDepOutputDirs: HashSet[String] = {
    val set = new HashSet[String]
    for (d <- deps) {
      val t = TargetManager.getTarget(d)
      set += t.outputDir.getAbsolutePath
      set ++= t.allDepOutputDirs
    }
    set
  }

  lazy val isCached: Boolean = checkIfCached

  private[target]
  var inputFiles: Seq[File] = null
  var depOutputDirs: Seq[File] = null

  def prepareInputFiles(inputs: Seq[FileLabel]) = labelToFiles(inputs)

  /**
   * Get dep targets output dirs.
   */
  def prepareDepOutputDirs(deps: Seq[TargetLabel]) : Seq[File] = {
    for (t <- deps) yield {
      val target = TargetManager.getTarget(t)
      target.outputDir
    }
  }

  def mkOutputDir() {
    if (!outputDir.isDirectory && !outputDir.mkdirs) {
      throw new TargetException(
          "Can not create output dir for target \"%s\": %s", name, outputDir.getPath)
    }
  }

  /**
   * Convert Seq[label: String] to Seq[File]
   */
  def labelToFiles(labels: Seq[FileLabel]): Seq[File] = {
    val files = labels.map(_.file)
    checkFiles(files)
    files
  }

  def checkFiles(files: Seq[File]) {
    for (f <- files) {
      if (!f.exists) {
        throw new TargetException("Target \"%s\" required input file \"%s\" not exists", targetName, f.getPath)
      }
    }
  }

  def runCmds(cmds: Seq[String], logFile: File, shFile: File, outputToStd: Boolean) {
    mkOutputDir
    val envCmds = Seq[String](
      "OUTPUT_DIR=\"%s\"" format stringEscape(outputDir.getAbsolutePath),
      "NAME=\"%s\"" format stringEscape(name),
      // TODO(timgreen): figure out a better way to pass array values
      "INPUTS=\"%s\"" format stringEscape(inputFiles.map(_.getAbsolutePath).mkString("|")),
      "DEP_OUTPUT_DIRS=\"%s\"" format stringEscape(depOutputDirs.map(_.getAbsolutePath).mkString("|")),
      "ALL_DEP_OUTPUT_DIRS=\"%s\"" format stringEscape(allDepOutputDirs.mkString("|")),
      ""
    )
    val splitArrayValueCmds = Seq[String](
      "OLD_IFS=\"$IFS\"",
      "IFS='|'",
      "INPUTS=( $INPUTS )",
      "DEP_OUTPUT_DIRS=( $DEP_OUTPUT_DIRS )",
      "ALL_DEP_OUTPUT_DIRS=( $ALL_DEP_OUTPUT_DIRS )",
      "IFS=\"$OLD_IFS\"",
      ""
    )

    writeCmdsToShellFile((envCmds ++ splitArrayValueCmds ++ cmds).mkString("\n"), shFile)

    val pb = new ProcessBuilder(
        "/bin/bash", shFile.getAbsolutePath)
    pb.directory(outputDir)
    pb.redirectErrorStream(true)

    val p = pb.start

    // Merge subprocess output
    val is = p.getInputStream
    val bytes = Array[Byte](100)
    val log = new java.io.FileOutputStream(logFile)
    try {
      var len = is.read(bytes)
      while(len != -1) {
        log.write(bytes, 0, len)
        if (outputToStd) {
          System.out.write(bytes, 0, len)
        }
        len = is.read(bytes)
      }
    } finally {
      log.close
    }

    if (p.waitFor != 0) {
      if (!outputToStd) {
        Source.fromFile(logFile).getLines foreach println
      }
      System.exit(p.exitValue)
    }
  }

  def writeCmdsToShellFile(cmds: String, shFile: File) {
    val p = new PrintStream(shFile)
    p.println(cmds)
    p.close
  }

  def stringEscape(str: String): String = {
    str.replaceAll("\"", "\\\"")
  }

  var isBuilded = false

  /**
   * One target is cached, if and only if
   *   - all deps is cahced
   *   - all input is not changed since last build
   */
  def checkIfCached(): Boolean = {
    val metaFile = cacheMetaFile
    if (!metaFile.exists) {
      return false
    }

    val cache = new Cache(metaFile)
    cache.read

    if (cache.deps != deps.map(_.targetName).toSet) {
      return false
    }
    for (d <- deps) {
      val t = TargetManager.getTarget(d)
      if (!t.isCached) {
        return false
      }
    }

    val inputs =
        for (i <- inputFiles) yield {
          i.getAbsolutePath -> i.lastModified
        }
    if (cache.inputs != inputs.toMap) {
      return false
    }

    return true
  }

  def saveCacheMeta {
    val cache = new Cache(cacheMetaFile)
    cache.deps ++= deps.map(_.targetName)
    cache.inputs ++= inputFiles.map((i) => {
      i.getAbsolutePath -> i.lastModified
    })
    cache.write
  }

  def buildLogFile = FileUtil.getBuildLogFile(path, name)
  def runLogFile = FileUtil.getRunLogFile(path, name)
  def buildShFile = FileUtil.getBuildShFile(path, name)
  def runShFile = FileUtil.getRunShFile(path, name)
  def cacheMetaFile = FileUtil.getBuildCacheMetaFile(path, name)

}
