package cook.config

import cook.path.PathRef
import cook.path.PathUtil

import java.util.concurrent.{ ConcurrentHashMap => JConcurrentHashMap }
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.io.Source
import scala.tools.nsc.io.Path


object ConfigType extends Enumeration {
  type ConfigType = Value
  val CookRootConfig, CookConfig, CookiConfig = Value
}

private[config] class ConfigRef(segments: List[String]) extends PathRef(segments) {

  lazy val configType = segments.last match {
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
      (packagePrefix :: segments.dropRight(1) :: segments.last.replace(".", "_") :: Nil) mkString "."
    case _ => (packagePrefix :: segments) mkString "."
  }

  lazy val configClassFilePath = PathUtil.cookRoot / (configClassName + ".scala")

  lazy val imports = loadImports
  val ImportPatternStr = """\s*//\s*#import("\(.*\)")\s*$"""
  val ImportP = ImportPatternStr.r
  private def loadImports: List[ConfigRef] = {
    Source.fromFile(p.path) getLines() takeWhile {
      _ matches ImportPatternStr
    } map { line =>
      val ImportP(importName) = line
      relativeConfigRef(importName)
    } toList
  }

  private def relativeConfigRef(importName: String) =
    ConfigRef(relativePathRefSegments(importName))
}

object ConfigRef {

  def apply(path: Path): ConfigRef = cache getOrElseUpdate (path.path, createConfigRef(path))
  def apply(segments: List[String]): ConfigRef = apply(PathUtil.relativeToRoot(segments: _*))

  private def createConfigRef(path: Path) =
    new ConfigRef(PathUtil.relativeToRoot(path))

  private val cache: mutable.ConcurrentMap[String, ConfigRef] =
    new JConcurrentHashMap[String, ConfigRef]
}
