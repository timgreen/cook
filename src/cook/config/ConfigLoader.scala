package cook.config

import java.lang.ClassLoader
import java.net.URLClassLoader


object ConfigLoader {

  def load(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]): Config = synchronized {
    val cl = getClassLoader(configRef, depConfigRefMap)
    val clazz = cl.loadClass(configRef.configClassFullName)
    clazz.asInstanceOf[Class[Config]].newInstance
  }

  private var cl: ClassLoader = this.getClass.getClassLoader
  private def getClassLoader(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]) = {
    val depConfigRefs = depConfigRefMap.values.toList
    val cp = (configRef :: depConfigRefs).map(_.configByteCodeDir.toURI.toURL).toArray
    cl = new URLClassLoader(cp, cl)
    cl
  }
}
