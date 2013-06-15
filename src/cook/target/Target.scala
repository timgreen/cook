package cook.target

import cook.error.ErrorTracking._
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

  import TargetStatus._
  private var _status: TargetStatus = Pending
  def status = _status
  def isResultReady = (_status == Cached) || (_status == Built)

  private[this] var _result: Option[R] = None
  def result: R = _result getOrElse {
    if (!isResultReady) {
      reportError("Can not call target %s.result, target not built yet. Do you miss deps", refName)
    }

    val r = resultFn(this)
    _result = Some(r)
    r
  }

  private def needBuild: Boolean = {
    val meta = buildMeta
    val cachedMeta = metaDb.get(ref.metaKey)
    meta != cachedMeta
  }

  private [cook] def build {
    assert(_status == Pending, "target should only be built once: " + refName)

    if (needBuild) {
      // cache hint
      _status = Cached
    } else {
      // need build
      ref.targetBuildDir.createDirectory(force = true)
      buildCmd(this)
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
  private [cook] def run(args: List[String] = List()): Int = {
    assert(isRunnable, "can not run a target without runCmd: " + refName)
    assert(isResultReady, "can not run a target that was not built yet: " + refName)
    runCmd.get(this, args)
  }
}

object Target {

  val DepMetaGroup = "deps"
  val InputMetaPrefix = "input"
}
