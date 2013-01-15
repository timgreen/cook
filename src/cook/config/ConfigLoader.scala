package cook.config

import java.net.URLClassLoader


object ConfigLoader {

  def load(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]): Config = {
    val cl = getClassLoader(configRef, depConfigRefMap)
    val clazz = cl.loadClass(configRef.configClassFullName)
    clazz.asInstanceOf[Class[Config]].newInstance
  }

  private def getClassLoader(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]) = {
    val depConfigRefs = depConfigRefMap.values.toList
    val cp = (configRef :: depConfigRefs).map(_.configByteCodeDir.toURI.toURL).toArray
    new URLClassLoader(cp, this.getClass.getClassLoader)
  }
}
