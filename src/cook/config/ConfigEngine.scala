package cook.config

import java.util.concurrent.{ ConcurrentHashMap => JConcurrentHashMap }
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.controlls.Exception._


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
      ConfigScalaSourceGenerator.generate(configRef)
      configRef.saveMeta
      configRef.imports foreach doGenerate
      configRef.configClassFilesDir.deleteRecursively
    }
  }

  private def doCompile(configRef: ConfigRef) {
    // NOTE(timgreen): we assmue if classes dir exist, it will always up-to-date.
    if (!configRef.configClassFilesDir.canRead) {
      configRef.imports foreach doCompile
      ConfigCompiler.compile(configRef)
    }
  }

}
