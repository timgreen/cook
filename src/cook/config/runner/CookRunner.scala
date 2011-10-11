package cook.config.runner

import java.io.File

import scala.collection.mutable.HashMap

import cook.config.parser._
import cook.config.parser.unit._
import cook.config.runner.value.Scope
import cook.error.ErrorMessageHandler
import cook.util.FileUtil

object CookRunner extends ErrorMessageHandler {

  import ConfigType._

  def run(configFile: File, configType: ConfigType): Scope = {
    if (configType == COOK) {
      checkAddCookRootScope
    }

    if (configToScopeMap.contains(configFile.getPath)) {
      return configToScopeMap(configFile.getPath)
    }

    if (!configFile.exists) {
      reportError("Cook Config file \"%s\" not found", configFile.getPath)
    }

    val scope = Scope()
    val config = CookParser.parse(configFile)
    wrapperError("Error when eval Cook config file: \"%s\"", configFile.getPath) {
      CookConfigEvaluator.eval(configType, FileUtil.relativeDirToRoot(configFile), scope, config)
    }

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
