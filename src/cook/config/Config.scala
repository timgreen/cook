package cook.config

import cook.config.dsl.ConfigContext
import cook.target.Target

import scala.tools.nsc.io.Path

trait Config {

  implicit val context: ConfigContext
  implicit def providePath: Path = context.path
  lazy val targets: Map[String, Target[_]] = context.targets map { t =>
    t.ref.name -> t
  } toMap
}
