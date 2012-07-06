package cook.config

import cook.util.PathUtil
import cook.util.HashManager

import java.util.concurrent.{ ConcurrentHashMap => JConcurrentHashMap }
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.tools.nsc.io.Path


object ConfigEngine {

  def load(configFile: Path): Config = {
    val hash = HashManager.hash(configFile)
    loadFromCache(configFile, hash) orElse
    loadFromClass(configFile, hash) getOrElse
    compileAndLoadFromSource(configFile, hash)
  }

  private val cache:mutable.ConcurrentMap[String, (String, Config)] =
    new JConcurrentHashMap[String, (String, Config)]
  private def loadFromCache(configFile: Path, hash: String): Option[Config] = {
    cache get configFile.path match {
      case Some((h, c)) if h == hash =>
        Some(c)
      case Some(_) =>
        cache -= configFile.path
        None
      case None =>
        None
    }
  }

  private def loadFromClass(configFile: Path, hash: String): Option[Config] = {
    // TODO(timgreen):
    None
  }

  private def compileAndLoadFromSource(configFile: Path, hash: String): Config = {
    // TODO(timgreen):
  }
}
