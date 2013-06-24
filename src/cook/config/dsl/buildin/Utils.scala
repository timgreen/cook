package cook.config.dsl.buildin

import cook.meta.Meta
import cook.meta.MetaHelper
import cook.ref.{ Ref, TargetRef }
import cook.target.{ Target, TargetResult }

import scala.reflect.io.{ Path => SPath }

trait Utils {

  def collectTargets(targets: List[Target[TargetResult]], targetRefs: List[Ref]): List[Target[TargetResult]] = {
    val m = targets map { t => t.refName -> t } toMap

    targetRefs map { ref => m(ref.refName) }
  }

  def collectTarget(targets: List[Target[TargetResult]], targetRef: Ref): Target[TargetResult] =
    collectTargets(targets, targetRef :: Nil).head

  def filesToMeta(group: String, files: Seq[SPath]): Meta = MetaHelper.buildFileMeta(group, files)
  def filesToMeta(group: String, file: SPath): Meta = filesToMeta(group, file :: Nil)
}
