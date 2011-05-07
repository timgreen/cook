package cook.config.runner.value.methods

object Installer {

  def install {
    // list
    ValueMethod.listMethods.put("join", Join)
    ValueMethod.listMethods.put("mkString", Join)

    // string
    ValueMethod.stringMethods.put("contains", Contains)
    ValueMethod.stringMethods.put("split", Split)
    ValueMethod.stringMethods.put("startsWith", StartsWith)
    ValueMethod.stringMethods.put("endsWith", EndsWith)
  }
}
