package cook.config.runner

import java.io.File

import scala.collection.mutable.HashMap

import cook.config.parser._
import cook.config.parser.unit._
import cook.config.runner.unit.RunnableUnitWrapper._
import cook.util.FileUtil

object ConfigType extends Enumeration {
  type ConfigType = Value
  val COOK, cooki, COOK_ROOT = Value
}

object CookRunner {

  import ConfigType._

  def run(configFile: File, configType: ConfigType): Scope = {
    if (configToScopeMap.contains(configFile.getPath)) {
      return configToScopeMap(configFile.getPath)
    }

    // TODO(timgreen): check if config type match the content
    // a. COOK_ROOT can not have function define or call "rule" function
    // b. cooki can not contain "include" command

    if (!configFile.exists) {
      throw new EvalException("Cook Config file \"%s\" not found".format(configFile.getPath))
    }

    val scope = Scope()
    CookParser.parse(configFile).run(FileUtil.relativeDirToRoot(configFile), scope)

    configToScopeMap.put(configFile.getPath, scope)
    scope
  }

  private[runner]
  val configToScopeMap: HashMap[String, Scope] = new HashMap[String, Scope]
}
