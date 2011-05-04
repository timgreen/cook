package cook.config.runner.buildin

import cook.config.runner.Scope
import cook.config.runner.value._

object Installer {

  def install {
    installConstValue
    installBuildinFunction
  }

  def installConstValue {
    Scope.ROOT_SCOPE.vars.put("true", BooleanValue.TRUE)
    Scope.ROOT_SCOPE.vars.put("false", BooleanValue.FALSE)
    Scope.ROOT_SCOPE.vars.put("null", new NullValue)
  }

  def installBuildinFunction {
    Scope.ROOT_SCOPE.funcs.put("glob",    Glob)
    Scope.ROOT_SCOPE.funcs.put("path",    Path)
    Scope.ROOT_SCOPE.funcs.put("include", Include)
    Scope.ROOT_SCOPE.funcs.put("genrule", Genrule)
    Scope.ROOT_SCOPE.funcs.put("echo",    Echo)
  }

}
