package cook.app.action

import cook.actor.Actors
import cook.app.MainHandler
import cook.path.Path
import cook.ref.RefManager
import cook.ref.TargetRef

object BuildAction {

  def run(args: List[String]) {
    if (args.isEmpty) {
      // TODO(timgreen): show help
      println("show help")
      return
    }

    val currentSegments = Path().currentSegments
    val targetRefs = args map { arg =>
      // TODO(timgreen): catch exception
      RefManager(currentSegments, arg).as[TargetRef]
    }

    val futureResults =
      for (targetRef <- targetRefs) yield {
        Actors.targetBuilder build targetRef
      }

    MainHandler.exec(futureResults: _*)
  }
}
