package cook.config.dsl.buildin

import cook.ref.{ Ref, TargetRef }
import cook.target.{ Target, TargetResult }

trait Utils {

  def collectTargets(targets: List[Target[TargetResult]], targetRefs: List[Ref]): List[Target[TargetResult]] = {
    val m = targets map { t => t.refName -> t } toMap

    targetRefs map { ref => m(ref.refName) }
  }
}
