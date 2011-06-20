package cook.config.runner.buildin

import cook.config.runner.value._

object Installer {

  def install {
    installConstValue
    installBuildinFunction
  }

  def installConstValue {
    Scope.ROOT_SCOPE("true") = BooleanValue("true", true)
    Scope.ROOT_SCOPE("false") = BooleanValue("false", false)
    Scope.ROOT_SCOPE("null") = NullValue("null")
  }

  def installBuildinFunction {
    val buildins = Array(
      AbsPath,
      Echo,
      Error,
      Genrule,
      Glob,
      Include,
      Label,
      Labels,
      Os,
      Path
    )

    for (b <- buildins) {
      Scope.ROOT_SCOPE(b.name) = b
    }
  }
}
