package cook.config

import java.lang.ClassLoader
import java.net.URL
import java.net.URLClassLoader
import scala.collection.mutable


object ConfigLoader {

  def load(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]): Config = synchronized {
    val cl = getClassLoader(configRef, depConfigRefMap)
    configRef.configType match {
      case ConfigType.CookConfig =>
        val clazz = cl.loadClass(configRef.configClassFullName)
        clazz.asInstanceOf[Class[Config]].newInstance
      case ConfigType.CookiConfig =>
        null
    }
  }

  private var cl: ClassLoader = this.getClass.getClassLoader
  private def getClassLoader(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]) = {
    val cp = buildCp(configRef, depConfigRefMap)
    if (configRef.configType == ConfigType.CookiConfig) {
      cachedCp(configRef.refName) = cp
    }
    cl = new URLClassLoader(cp.toArray, cl)
    cl
  }

  private def buildCp(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]): Set[URL] = {
    val depConfigRefs = depConfigRefMap.values.toList
    val sets: List[Set[URL]] = if (configRef.configType == ConfigType.CookiConfig) {
      configRef.includes map { i => cachedCp(i.ref.refName) } toList
    } else {
      depConfigRefs map { r => cachedCp(r.refName) }
    }

    (Set(configRef.configByteCodeDir.toURI.toURL) :: sets).flatten.toSet
  }

  private val cachedCp = mutable.Map[String, Set[URL]]()
}
