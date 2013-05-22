package cook.app.action

import cook.actor.Actors
import cook.app.MainHandler
import cook.path.Path
import cook.ref.RefManager
import cook.ref.TargetRef

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

    MainHandler.exec(futureResults: _*)
  }
}
