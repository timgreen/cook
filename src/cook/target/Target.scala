package cook.target

import cook.console.ops._
import cook.error._
import cook.meta.Meta
import cook.meta.db.DbProvider.{ db => metaDb }
import cook.ref.TargetRef


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


  import TargetStatus._
  private var _status: TargetStatus = Pending
  def status = _status
  def isResultReady = (_status == Cached) || (_status == Built)

  private[this] var _result: Option[R] = None
  private [cook] def buildResult(depTargets: List[Target[TargetResult]]): R = {
    assert(_result.isEmpty, "result should only be built once: " + refName)

    if (!isResultReady) {
      reportError {
        "Can not call target " :: strong(refName) :: ".result, target not built yet. " ::
        "You might miss deps"
      }
    }

    val r = resultFn(this, depTargets)
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

  private [cook] def build(depTargets: List[Target[TargetResult]]) {
    assert(_status == Pending, "target should only be built once: " + refName)

    if (needBuild) {
      // cache hint
      _status = Cached
    } else {
      // need build
      buildDir.createDirectory(force = true)
      buildCmd(this, depTargets)
      _status = Built
      val meta = buildMeta
      metaDb.put(ref.metaKey, meta)
    }
  }

  private [cook] def buildMeta: Meta = {
    val inputMeta = inputMetaFn(this)
    val depsMeta = new Meta
    deps foreach { dep =>
      depsMeta.add(Target.DepMetaGroup, dep.refName, metaDb.get(dep.metaKey).hash)
    }
    inputMeta.withPrefix(Target.InputMetaPrefix) + depsMeta
  }

  def isRunnable = runCmd.isDefined
  private [cook] def run(args: List[String] = Nil): Int = {
    assert(isRunnable, "can not run a target without runCmd: " + refName)
    assert(isResultReady, "can not run a target that was not built yet: " + refName)
    runCmd.get(this, args)
  }
}

object Target {

  val DepMetaGroup = "deps"
  val InputMetaPrefix = "input"
}
