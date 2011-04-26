package cook.config.runner.buildin

import cook.config.runner.Scope

object Installer {

  def initBuildinFunction {
    Scope.ROOT_SCOPE.funcs.put("glob",    Glob)
    Scope.ROOT_SCOPE.funcs.put("path",    Path)
    Scope.ROOT_SCOPE.funcs.put("include", Include)
    Scope.ROOT_SCOPE.funcs.put("genrule", Genrule)
    Scope.ROOT_SCOPE.funcs.put("echo",    Echo)
  }

}
