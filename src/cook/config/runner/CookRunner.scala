package cook.config.runner

import java.io.File

import scala.collection.mutable.HashMap

import cook.config.parser._
import cook.config.parser.unit._
import cook.config.runner.unit.RunnableUnitWrapper._

object CookRunner {

  // TODO(timgreen): DO WITH COOK_ROOT
  def run(path: String, configFile: File): Scope = {
    if (configToScopeMap.contains(path)) {
      return configToScopeMap(path)
    }

    val scope = Scope()
    CookParser.parse(configFile).run(path,  scope)

    configToScopeMap.put(path, scope)
    scope
  }

  private[runner]
  val configToScopeMap: HashMap[String, Scope] = new HashMap[String, Scope]
}
