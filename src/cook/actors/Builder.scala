package cook.actors

import scala.collection.mutable.HashSet
import scala.collection.mutable.Queue

import cook.app.console.CookConsole
import cook.target._
import cook.util._

object Builder {

  def build(targetLabels: Seq[TargetLabel]) {
    val analyst = Analyst(targetLabels: _*)
    CookConsole.mark('buildStatus)
    updateBuildStatus(analyst)
    while (analyst.nonEmpty) {
      val targetName = analyst.get.get
      analyst.setBuilding(targetName)

      updateBuildStatus(analyst)

      val target = TargetManager.getTarget(targetName)
      target.build

      if (target.isCached) {
        analyst.setCached(targetName)
      } else {
        analyst.setBuilt(targetName)
      }
      updateBuildStatus(analyst)
    }
    updateBuildStatus(analyst)
    CookConsole.println("")
  }

  private
  def updateBuildStatus(analyst: Analyst) {
    CookConsole.clearToMark('buildStatus)

    if (analyst.building.nonEmpty) {
      CookConsole.cookPercentage(analyst.percentage)
      CookConsole.print("Building ")
      CookConsole.control(Console.CYAN)
      CookConsole.print("%d", analyst.building.size)
      CookConsole.reset
      CookConsole.print(", ")
    }
    CookConsole.print("Cached ")
    CookConsole.control(Console.CYAN)
    CookConsole.print("%d", analyst.cached.size)
    CookConsole.reset
    CookConsole.print(", Built ")
    CookConsole.control(Console.CYAN)
    CookConsole.print("%d", analyst.built.size)
    CookConsole.reset
    if (analyst.building.nonEmpty) {
      CookConsole.println(":")
      CookConsole.control(Console.GREEN)
      CookConsole.println(analyst.building.mkString(", "))
      CookConsole.reset
    }
  }
}
