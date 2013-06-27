package cook.app.action

import cook.actor.Actors
import cook.app.Global
import cook.app.MainHandler
import cook.console.Console
import cook.path.Path
import cook.ref.RefManager
import cook.ref.TargetRef

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._

object RunAction {

  def run(targetRefName: String, args: List[String]) {
    val currentSegments = Path().currentSegments
    val targetRef = RefManager(currentSegments, targetRefName).as[TargetRef]

    val futureTargetAndResult = Actors.targetBuilder build targetRef
    implicit val ec = Global.workerDispatcher
    val futureTargetAndDeps = futureTargetAndResult map { case (t, _) =>

      val fs = t.deps.toList map Actors.targetManager.getTarget
      val depTargets = Await.result(Future.sequence(fs), Duration.Inf)

      t -> depTargets
    }

    MainHandler.exec(futureTargetAndDeps)

    Console.runTarget(targetRefName)
    val (t, depTargets) = Await.result(futureTargetAndDeps, Duration.Inf)
    // TODO(timgreen):
    //  - exit code
    t.run(depTargets, args)
  }
}
