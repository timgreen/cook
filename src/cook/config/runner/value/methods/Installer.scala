package cook.config.runner.value.methods

object Installer {

  def install {
    ValueMethod.listMethods.put("join", Join)
    ValueMethod.listMethods.put("mkString", Join)
  }
}
