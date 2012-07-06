package cook.config

import scala.tools.nsc.io.Path

case class Dependence(classPaths: Seq[Path], imports: Seq[Path])

trait Config {
  val deps: Dependence
}
