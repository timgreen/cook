package cook.config.runner

import java.io.File

import scala.collection.mutable.HashMap

import cook.config.parser._
import cook.config.parser.unit._

object CookRunner {

  def run(path: String, configFile: File): Scope = {
    if (configToScopeMap.contains(path)) {
      return configToScopeMap(path)
    }

    val includedScope = rootScope.clone.asInstanceOf[Scope]
    val scope = Scope()

    val cookConfig = CookParser.parse(configFile)
    // TODO(timgreen):

    configToScopeMap.put(path, scope)
    scope
  }

  private[runner]
  val configToScopeMap = new HashMap[String, Scope]

  def rootScope = configToScopeMap("COOK_ROOT")
}
