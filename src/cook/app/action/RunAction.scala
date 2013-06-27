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
    MainHandler.exec(futureTargetAndResult)

    Console.runTarget(targetRefName)
    val (t, _) = Await.result(futureTargetAndResult, Duration.Inf)
    // TODO(timgreen):
    //  - exit code
    t.run(args)
  }
}
