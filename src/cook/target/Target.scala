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
import cook.config.runner._
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
    val preRun: FunctionValue,
    val errorWhenNoOutput: Boolean) {

  var values = new HashMap[String, Value]

  def targetName(): String = {
    path + ":" + name
  }

  /**
   * Only executeable target can be run by "cook run"
   */
  def isExecutable = (exeCmds.nonEmpty) || (preRun != null)

  def execute(args: Array[String]): Int = {
    if (!isExecutable) {
      throw new TargetException("Target \"%s\" is not executeable", targetName)
    }

    val c = if (exeCmds.nonEmpty) {
      exeCmds
    } else {
      evalFunction(preRun).toListStr(
          "Return value for target(\"%s\").preRun() should be String List", targetName)
    }

    cleanBeforeExecute
    runCmds(c, runLogFile, runShFile, true, args: _*)
  }

  /**
   * Target it self doesn't care about deps, it only care about the input and output,
   * deps will be done by Target Manager.
   */
  def build(): Int = {
    // 0. save current timestamp
    // 1. check whether all inputs is ready
    // 2. run cmd
    // 3. Save cache data

    val currentTimestamp = new Date().getTime
    inputFiles = prepareInputFiles(inputs)
    if (isBuilded) {
      throw new TargetException(
          "One target should never been build twice: target \"%s\"",
          targetName)
    }

    val c = if (cmds.nonEmpty) {
      cmds
    } else {
      evalFunction(preBuild).toListStr(
          "Return value for target(\"%s\").preBuild() should be String List", targetName)
    }
    if (!isCached) {
      cleanBeforeBuild
      val exitValue = runCmds(c, buildLogFile, buildShFile, false)

      if (exitValue != 0 || checkErrorWhenNoOutput) {
        Source.fromFile(buildLogFile).getLines foreach println
        return 1
      }

      saveCacheMeta
    }

    isBuilded = true

    if (postBuild != null) {
      evalFunction(postBuild)
    }

    0
  }

  private def checkErrorWhenNoOutput(): Boolean = {
    if (!errorWhenNoOutput) return false
    val l = outputDir.list
    return if ((l == null) || l.isEmpty) {
      println("Error: target '%s' has no output".format(targetName))
      true
    } else {
      false
    }
  }

  def outputDir = FileUtil.getBuildOutputDir(path, name)

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

  private var inputFiles: Seq[File] = null

  private def prepareInputFiles(inputs: Seq[FileLabel]) = labelToFiles(inputs)

  /**
   * Get dep targets output dirs.
   */
  private def prepareDepOutputDirs(deps: Seq[TargetLabel]) : Seq[File] = {
    for (t <- deps) yield {
      val target = TargetManager.getTarget(t)
      target.outputDir
    }
  }

  private def mkOutputDir() {
    if (!outputDir.isDirectory && !outputDir.mkdirs) {
      throw new TargetException(
          "Can not create output dir for target \"%s\": %s", name, outputDir.getPath)
    }
  }

  /**
   * Convert Seq[label: FileLabel] to Seq[File]
   */
  private def labelToFiles(labels: Seq[FileLabel]): Seq[File] = {
    val files = labels.map(_.file)
    checkFiles(files)
    files
  }

  private def checkFiles(files: Seq[File]) {
    for (f <- files) {
      if (!f.exists) {
        throw new TargetException("Target \"%s\" required input file \"%s\" not exists", targetName, f.getPath)
      }
    }
  }

  private def runCmds(cmds: Seq[String], logFile: File, shFile: File, outputToStd: Boolean, args: String*): Int = {
    mkOutputDir
    val envCmds = Seq[String](
      "OUTPUT_DIR=\"%s\"" format stringEscape(outputDir.getAbsolutePath),
      "NAME=\"%s\"" format stringEscape(name)
    )

    writeCmdsToShellFile((envCmds ++ cmds).mkString("\n"), shFile)

    val pb = new ProcessBuilder(("/bin/bash" :: shFile.getAbsolutePath :: args.toList) : _*)
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

    p.waitFor
  }

  private def writeCmdsToShellFile(cmds: String, shFile: File) {
    val p = new PrintStream(shFile)
    p.println(cmds)
    p.close
  }

  private def stringEscape(str: String): String = {
    str.replaceAll("\"", "\\\"")
  }

  private var isBuilded = false

  /**
   * One target is cached, if and only if
   *   - all deps is cahced
   *   - all input is not changed since last build
   */
  private def checkIfCached(): Boolean = {
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

  private def saveCacheMeta {
    val cache = new Cache(cacheMetaFile)
    cache.deps ++= deps.map(_.targetName)
    cache.inputs ++= inputFiles.map((i) => {
      i.getAbsolutePath -> i.lastModified
    })
    cache.write
  }

  private def evalFunction(functionValue: FunctionValue): Value = {
    val args = Scope(functionValue.scope)
    val argName = functionValue.argsDef.names.head
    args(argName) = TargetLabelValue(argName, new TargetLabel(path, ":" + name))
    FunctionValueCallEvaluator.eval(ConfigType.COOK, path, args, functionValue)
  }

  def buildLogFile = FileUtil.getBuildLogFile(path, name)
  def runLogFile = FileUtil.getRunLogFile(path, name)
  def buildShFile = FileUtil.getBuildShFile(path, name)
  def runShFile = FileUtil.getRunShFile(path, name)
  def cacheMetaFile = FileUtil.getBuildCacheMetaFile(path, name)

  def cleanBeforeBuild {
    DeleteUtil.deleteRecursively(outputDir)
    DeleteUtil.deleteRecursively(buildLogFile)
    DeleteUtil.deleteRecursively(buildShFile)
    DeleteUtil.deleteRecursively(cacheMetaFile)
  }

  def cleanBeforeExecute {
    DeleteUtil.deleteRecursively(runLogFile)
    DeleteUtil.deleteRecursively(runShFile)
  }
}
