package cook.target

import cook.config.ConfigRef
import cook.console.ops._
import cook.error._
import cook.meta.{ Meta, MetaHelper }
import cook.meta.db.DbProvider.{ db => metaDb }
import cook.ref.TargetRef
import cook.util.GlobScanner


object TargetStatus extends Enumeration {
  type TargetStatus = Value
  val Pending, Cached, Built = Value
}


abstract class Target[+R <: TargetResult](
    val ref: TargetRef,
    private[this] val buildCmd: TargetBuildCmd[R],
    private[this] val resultFn: TargetResultFn[R],
    private[this] val inputMetaFn: TargetMetaFn[R],
    private[this] val runCmd: Option[TargetRunCmd[R]],
    val deps: Seq[TargetRef]) {

  def refName = ref.refName
  def buildDir = ref.targetBuildDir
  def runDir = ref.targetRunDir


  import TargetStatus._
  private var _status: TargetStatus = Pending
  def status = _status
  def isResultReady = (_status == Cached) || (_status == Built)

  private var _depTargets: Option[List[Target[TargetResult]]] = None
  def depTargets: List[Target[TargetResult]] = _depTargets getOrError {
    "Can not call target " :: strong(refName) :: ".depTargets, deps not ready yet."
  }
  private [cook] def setDepTargets(depTargets: List[Target[TargetResult]]) {
    assert(_depTargets.isEmpty, "depTargets should only be set once: " + refName)
    val f = deps.map(_.refName).toSet == depTargets.map(_.refName).toSet
    assert(f, "depTargets should equal to deps: " + refName)
    _depTargets = Some(depTargets)
  }

  private[this] var _result: Option[R] = None
  private [cook] def buildResult: R = {
    assert(_result.isEmpty, "result should only be built once: " + refName)

    if (!isResultReady) {
      reportError {
        "Can not call target " :: strong(refName) :: ".result, target not built yet. " ::
        "You might miss deps"
      }
    }

    val r = resultFn(this)
    _result = Some(r)
    r
  }
  def result: R = _result getOrError {
    "Can not call target " :: strong(refName) :: ".result, result not ready yet."
  }

  private def needBuild: Boolean = {
    val meta = buildMeta
    val cachedMeta = metaDb.get(ref.metaKey)
    meta != cachedMeta
  }

  private [cook] def build {
    assert(_status == Pending, "target should only be built once: " + refName)

    if (needBuild) {
      // need build
      buildDir.deleteRecursively
      buildDir.createDirectory(force = true)
      ref.buildLogFile.deleteIfExists
      buildCmd(this)
      _status = Built
      val meta = buildMeta
      metaDb.put(ref.metaKey, meta)
    } else {
      // cache hint
      _status = Cached
    }
  }

  private [cook] def buildMeta: Meta = {
    // dep
    val depsMeta = new Meta
    deps foreach { dep =>
      depsMeta.add(Target.DepMetaGroup, dep.refName, metaDb.get(dep.metaKey).hash)
    }
    // config
    val configMeta = new Meta
    val defineConfigRefName = ConfigRef.defineConfigRefNameForTarget(refName)
    val configKey = ConfigRef.configByteCodeMetaKeyFor(defineConfigRefName)
    configMeta.add(Target.ConfigMetaGroup, "config", metaDb.get(configKey).hash)
    // input
    val inputMeta = inputMetaFn(this).withPrefix(Target.InputMetaPrefix)
    // target
    val targetMeta = if (buildDir.exists) {
      val targets = GlobScanner(buildDir, "**" :: Nil)
      MetaHelper.buildFileMeta(Target.TargetMetaGroup, targets)
    } else {
      new Meta
    }

    //
    depsMeta + configMeta + inputMeta + targetMeta
  }

  def isRunnable = runCmd.isDefined
  def run(args: List[String] = Nil): Int = {
    assert(isRunnable, "can not run a target without runCmd: " + refName)
    assert(isResultReady, "can not run a target that was not built yet: " + refName)
    runDir.deleteRecursively
    runDir.createDirectory(force = true)
    ref.runLogFile.deleteIfExists
    runCmd.get(this, args)
  }
}

object Target {

  val DepMetaGroup = "deps"
  val ConfigMetaGroup = "config"
  val TargetMetaGroup = "target"
  val InputMetaPrefix = "input"
}
