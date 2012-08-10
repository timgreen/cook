package cook.config

import cook.util.PathUtil

import java.util.concurrent.{ ConcurrentHashMap => JConcurrentHashMap }
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.controlls.Exception._
import scala.tools.nsc.io.Path


/**
 * Compile and load and cache cook config.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
object ConfigEngine {

  case ConfigWithHash(configRef: ConfigRef)

  def load(configRef: ConfigRef): Config = {
    loadFromCache(configRef) orElse
      loadFromClass(configRef) getOrElse
      compileAndLoadFromSource(configRef)
  }

  private val cache: mutable.ConcurrentMap[String, ConfigWithHash] =
    new JConcurrentHashMap[String, ConfigWithHash]

  private def loadFromCache(configRef: ConfigRef): Option[Config] = {
    cache get configRef.p.path match {
      case Some(ConfigWithHash(c, h)) =>
        if (h == configRef.hash) {
          Some(c)
        } else {
          cache -= configRef.p.path
          None
        }
      case None =>
        None
    }
  }

  private def loadFromClass(configRef: ConfigRef): Option[Config] = {
    val classFilePath = configRef.classFilePath
    if (classFilePath.lastModified > configRef.p.lastModified) {
      allCatch.opt {
        // TODO(timgreen): load class
      }
    } else {
      None
    }
  }

  private def compileAndLoadFromSource(configFile: Path, hash: String): Config = {
    // TODO(timgreen):
  }
}
