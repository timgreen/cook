package cook.target

import java.io.File
import java.util.Date

import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.immutable.VectorBuilder
import scala.collection.mutable.HashSet
import scala.io.Source

import org.apache.tools.ant.DirectoryScanner

import cook.util._

class Target(
    val path: String,
    val name: String,
    val cmds: Seq[String],
    val inputs: Seq[FileLabel],
    val deps: Seq[TargetLabel],
    val exeCmds: Seq[String]) {

  def targetName(): String = {
    path + ":" + name
  }

  /**
   * Only executeable target can be run by "cook run"
   */
  def isExecutable = exeCmds.nonEmpty

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
      println("Building target \"%s\"".format(targetName))
      runCmds(cmds)
    } else {
      println("Cached   target \"%s\"".format(targetName))
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

  def runCmds(cmds: Seq[String]) {
    mkOutputDir
    val splitArrayValueCmds = Seq[String](
      "OLD_IFS=\"$IFS\"",
      "IFS='|'",
      "INPUTS=( $INPUTS )",
      "DEP_OUTPUT_DIRS=( $DEP_OUTPUT_DIRS )",
      "ALL_DEP_OUTPUT_DIRS=( $ALL_DEP_OUTPUT_DIRS )",
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

    val p = pb.start

    // Merge subprocess output
    val is = p.getInputStream
    val bytes = Array[Byte](100)
    val log = new java.io.FileOutputStream(logFile)
    log.write(((splitArrayValueCmds ++ cmds).mkString(";") + "\n").getBytes)
    try {
      while(is.read(bytes) != -1) {
        log.write(bytes)
      }
    } finally {
      log.close
    }
    p.waitFor

    if (p.exitValue != 0) {
      Source.fromFile(logFile).getLines foreach println
      System.exit(p.exitValue)
    }
  }

  var isBuilded = false

  def getCacheMetaFile =
      FileUtil.getBuildCacheMetaFile(path, name)

  /**
   * One target is cached, if and only if
   *   - all deps is cahced
   *   - all input is not changed since last build
   */
  def checkIfCached(): Boolean = {
    val metaFile = getCacheMetaFile
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
    val cache = new Cache(getCacheMetaFile)
    cache.deps ++= deps.map(_.targetName)
    cache.inputs ++= inputFiles.map((i) => {
      i.getAbsolutePath -> i.lastModified
    })
    cache.write
  }

  def logFile(): File = {
    FileUtil.getBuildLogFile(path, name)
  }
}
