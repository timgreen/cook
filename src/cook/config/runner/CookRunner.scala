package cook.config.runner

import java.io.File

import scala.collection.mutable.HashMap

import cook.config.parser._
import cook.config.parser.unit._
import cook.config.runner.value.Scope
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

    if (!configFile.exists) {
      throw new EvalException("Cook Config file \"%s\" not found", configFile.getPath)
    }

    val scope = Scope()

    val config = CookParser.parse(configFile)
    checkConfigType(configFile, config, configType)

    CookConfigEvaluator.eval(FileUtil.relativeDirToRoot(configFile), scope, config)

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

  def checkConfigType(configFile: File, config: CookConfig, configType: ConfigType) {
    def isIncludeFunctionCall(s: Statement): Boolean = s match {
      case FuncCall(name, _) => name == "include"
      case _ => false
    }

    def notContainIncludeCall(s: Statement): Boolean = s match {
      case FuncCall(name, _) => name != "include"
      case FuncDef(_, _, statements, _) => statements.forall(notContainIncludeCall)
      case _ => true
    }

    // a. COOK_ROOT can only call function "include"
    // b. cooki can not call function "include"
    // c. COOK has no limitation
    configType match {
      case ConfigType.COOK_ROOT =>
        if (!config.statements.forall(isIncludeFunctionCall)) {
          throw new EvalException(
              "COOK_ROOT \"%s\" can only call function \"include\"",
              configFile.getPath)
        }
      case ConfigType.cooki =>
        if (!config.statements.forall(notContainIncludeCall)) {
          throw new EvalException(
              "cooki file \"%s\" can not call function \"include\"",
              configFile.getPath)
        }
      case ConfigType.COOK =>  // No check needed
    }
  }
}
