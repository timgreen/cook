package cook.app.action

import cook.path.Path
import cook.ref.RefManager
import cook.ref.TargetRef
import cook.actor.Actors

object BuildAction extends ActionBase {

  override def run(args: List[String]) {
    val currentSegments = Path().currentSegments
    val targetRefs = args map { arg =>
      // TODO(timgreen): catch exception
      val ref = RefManager(currentSegments, arg)
      ref.asInstanceOf[TargetRef]
    }

    val futureResults =
      for (targetRef <- targetRefs) yield {
        Actors.targetBuilder build targetRef
      }

    // TODO(timgreen): wait all results
  }
}
