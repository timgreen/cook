package cook.config

import cook.path.PathUtil

import java.util.concurrent.{ ConcurrentHashMap => JConcurrentHashMap }
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.control.Exception._


/**
 * Compile and load and cache cook config.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
object ConfigEngine {

  case class ConfigWithHash(configRef: ConfigRef)

  def init {
    // NOTE(timgreen): if COOK_ROOT changed, all bytecode need re-generated.
    // TODO(timgreen): better recompile detect
    if (ConfigRef.rootConfigRef.shouldGenerateScala) {
      PathUtil().cookConfigByteCodeDir.deleteRecursively
    }
  }

  def load(configRef: ConfigRef): Config = {
    loadFromCache(configRef) orElse
      loadFromClass(configRef) getOrElse
      compileAndLoadFromSource(configRef)
  }

  private [config] val cache: mutable.ConcurrentMap[String, ConfigWithHash] =
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
        null
      }
    } else {
      None
    }
  }

  private def compileAndLoadFromSource(configRef: ConfigRef): Config = {
    doGenerate(configRef)
    doCompile(configRef)
    // TODO(timgreen):
    null
  }

  private def doGenerate(configRef: ConfigRef) {
    if (configRef.shouldGenerateScala) {
      ConfigGenerator.generate(configRef)
      configRef.saveMeta
      configRef.imports foreach doGenerate
      configRef.configByteCodeDir.deleteRecursively
    }
  }

  private def doCompile(configRef: ConfigRef) {
    // NOTE(timgreen): we assmue if bytecode dir exist, it will always up-to-date.
    if (!configRef.configByteCodeDir.canRead) {
      configRef.imports foreach doCompile
      ConfigCompiler.compile(configRef)
    }
  }

}
