package cook.config

import cook.config.dsl.ConfigContext

import scala.tools.nsc.io.Path

trait Config {

  implicit val context: ConfigContext
  implicit def providePath: Path = context.path
}
