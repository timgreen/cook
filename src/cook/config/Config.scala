package cook.config

import cook.util.HashManager
import cook.util.PathUtil

import scala.tools.nsc.io.Path


object ConfigType {
  val CookRootConfig
  val CookConfig
  val CookiConfig
}

case class Dependence(imports: Seq[ConfigRef])

trait Config {
  val deps: Dependence
}

class ConfigRef(parts: Seq[String]) {

  lazy val p: Path = parts.fold(PathUtil.cookRoot. _ / _)
  lazy val configType = parts.last match {
    case "COOK_ROOT" => ConfigType.CookRootConfig
    case "COOK" => ConfigType.CookConfig
    case f if f.endsWith(".cooki") => ConfigType.CookiConfig
    case _ =>
      // TODO(timgreen): error
      throw new Exception("error")
  }

  val packagePrefix = "COOK_CONFIG_PACKAGE"
  lazy val configClassName = configType match {
    case ConfigType.CookiConfig =>
      (packagePrefix :: parts.dropRight(1) :: parts.last.replace(".", "_") :: nil) mkString "."
    case _ => (packagePrefix :: parts) mkString "."
  }

  lazy val configClassFilePath = PathUtil.cookRoot / (configClassName + ".scala")

  def hash = HashManager.hash(p)
}

object Config {

  def apply(path: Path): ConfigRef = {
    val parts = PathUtil.relativeToRoot(path).split("/")
    new ConfigRef(parts)
  }
}
