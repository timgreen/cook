package cook.config

import java.net.URLClassLoader


object ConfigLoader {

  def load(configRef: ConfigRef): Config = {
    val cl = getClassLoader(configRef)
    val clazz = cl.loadClass(configRef.configClassFullName)
    clazz.asInstanceOf[Class[Config]].newInstance
  }

  private def getClassLoader(configRef: ConfigRef) = {
    val refs = Set(
      configRef,
      ConfigRef.rootConfigRef
    ) ++ configRef.imports.map(_.ref) ++ ConfigRef.rootConfigRef.mixins
    val cp = refs.map(_.configByteCodeDir.toURI.toURL).toArray
    new URLClassLoader(cp, this.getClass.getClassLoader)
  }
}
