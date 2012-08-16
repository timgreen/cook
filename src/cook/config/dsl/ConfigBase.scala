package cook.config.dsl

import scala.tools.nsc.io.Path

trait ConfigBase {

  implicit def provideContext: ConfigContext
  implicit def providePath: Path = provideContext.path

}
