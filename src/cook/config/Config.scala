package cook.config

import cook.config.dsl.ConfigContext

import scala.tools.nsc.io.Path

trait Config {

  implicit def provideContext: ConfigContext
  implicit def providePath: Path = provideContext.path
}
