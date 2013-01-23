package cook.config


/**
 * Generate, compile and load config class.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
private[cook] object ConfigEngine {

  def load(configRef: ConfigRef, depConfigRefs: List[ConfigRef]): Config = {
    val map: Map[String, ConfigRef] = depConfigRefs map { r => r.refName -> r } toMap

    doGenerate(configRef, map)
    doCompile(configRef, map)
    doLoad(configRef, map)
  }

  private def doGenerate(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]) {
    if (shouldRegenerateScala(configRef)) {
      ConfigGenerator.generate(configRef, depConfigRefMap)
      configRef.configByteCodeDir.deleteRecursively
      //configRef.saveMeta
    }
  }

  private def doCompile(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]) {
    if (shouldRecompileScala(configRef)) {
      // ConfigCompiler.compile(configRef)
    }
  }

  private def doLoad(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]): Config = {
    val c = ConfigLoader.load(configRef, depConfigRefMap)
    // cache(configRef.fileRef.toPath.path) = ConfigWithHash(c, configRef.hash)
    c
  }

  private def shouldRegenerateScala(configRef: ConfigRef): Boolean = {
    true
  }

  private def shouldRecompileScala(configRef: ConfigRef): Boolean = {
    // NOTE(timgreen): we assmue if bytecode dir exist, it will always up-to-date.
    !configRef.configByteCodeDir.canRead
  }
}
