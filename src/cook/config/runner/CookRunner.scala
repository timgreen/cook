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
    if (configType == COOK) {
      checkAddCookRootScope
    }

    if (configToScopeMap.contains(configFile.getPath)) {
      return configToScopeMap(configFile.getPath)
    }

    println("CookRunner run \"%s\"".format(configFile.getPath))

    // TODO(timgreen): check if config type match the content
    // a. COOK_ROOT can only call function "include"
    // b. cooki can not call function "include"

    if (!configFile.exists) {
      throw new EvalException("Cook Config file \"%s\" not found", configFile.getPath)
    }

    val scope = Scope()
    CookParser.parse(configFile).run(FileUtil.relativeDirToRoot(configFile), scope)

    configToScopeMap.put(configFile.getPath, scope)
    scope
  }

  private[runner]
  val configToScopeMap: HashMap[String, Scope] = new HashMap[String, Scope]

  def checkAddCookRootScope() {
    val cookRoot = FileUtil.getCookRootFile
    if (configToScopeMap.contains(cookRoot.getPath)) {
      return
    }

    Scope.ROOT_SCOPE.merge(run(cookRoot, COOK_ROOT))
  }
}
