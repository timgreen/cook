package cook.config.dsl.buildin

import cook.config.dsl.ConfigContext
import cook.util.GlobScanner

import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path

trait Glob {

  def glob(includes: String*)(implicit context: ConfigContext): List[Path] = {
    GlobScanner(
      dir = context.path,
      includes = includes,
      fileOnly = true
    ) toList
  }

  def glob(includes: List[String], excludes: List[String])
    (implicit context: ConfigContext): List[Path] = {
    GlobScanner(
      dir = context.path,
      includes = includes,
      excludes = excludes,
      fileOnly = true
    ) toList
  }

  def glob(includes: List[String], baseDir: Directory,
    excludes: Seq[String] = List(), fileOnly: Boolean = true): List[Path] = {
    GlobScanner(
      dir = baseDir,
      includes = includes,
      excludes = excludes,
      fileOnly = fileOnly
    ) toList
  }
}
