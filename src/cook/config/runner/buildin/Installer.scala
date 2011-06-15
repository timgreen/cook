package cook.config.runner.buildin

import cook.config.runner.value._

object Installer {

  def install {
    installConstValue
    installBuildinFunction
  }

  def installConstValue {
    Scope.ROOT_SCOPE("true") = BooleanValue.TRUE
    Scope.ROOT_SCOPE("false") = BooleanValue.FALSE
    Scope.ROOT_SCOPE("null") = NullValue()
  }

  def installBuildinFunction {
    Scope.ROOT_SCOPE("abspath") = AbsPath
    Scope.ROOT_SCOPE("echo")    = Echo
    Scope.ROOT_SCOPE("error")   = Error
    Scope.ROOT_SCOPE("genrule") = Genrule
    Scope.ROOT_SCOPE("glob")    = Glob
    Scope.ROOT_SCOPE("include") = Include
    Scope.ROOT_SCOPE("label")   = Label
    Scope.ROOT_SCOPE("labels")  = Labels
    Scope.ROOT_SCOPE("os")      = Os
    Scope.ROOT_SCOPE("path")    = Path
  }

}
