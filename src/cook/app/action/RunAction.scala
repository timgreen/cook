package cook.app.action

import cook.actor.Actors
import cook.app.Global
import cook.app.MainHandler
import cook.console.Console
import cook.path.Path
import cook.ref.RefManager
import cook.ref.TargetRef

object RunAction extends ActionBase {

  override def run(args: List[String]) {
    assert(args.size == 1, "Can only run one target at once.")
    val targetRefName = args.head

    val currentSegments = Path().currentSegments
    val targetRef = RefManager(currentSegments, targetRefName).as[TargetRef]

    val futureTargetAndResult = Actors.targetBuilder build targetRef
    implicit val ec = Global.workerDispatcher

    MainHandler.exec(futureTargetAndResult map { case (t, r) =>
      import scala.concurrent.Await
      import scala.concurrent.Future
      import scala.concurrent.duration._

      val fs = t.deps.toList map Actors.targetManager.getTarget
      Console.runTarget(targetRefName)
      val depTargets = Await.result(Future.sequence(fs), Duration.Inf)
      // TODO(timgreen):
      //  - exit code
      //  - args
      t.run(depTargets, Nil)
    })
  }
}
