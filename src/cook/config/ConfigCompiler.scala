package cook.config


object ConfigCompiler {

  def compile(configRef: ConfigRef) {
    configRef.configClassFilesDir.createDirectory()
    // TODO(timgreen):
  }
}
