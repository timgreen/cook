package cook.actors

import scala.collection.mutable.HashSet
import scala.collection.mutable.Queue

import cook.app.console.CookConsole
import cook.target._
import cook.util._

object Runner {

  def run(targetLabel: TargetLabel, args: Array[String]) {
    CookConsole.println("Running target \"%s\"", targetLabel.targetName)
    val target = TargetManager.getTarget(targetLabel)
    target.execute(args)
  }
}

