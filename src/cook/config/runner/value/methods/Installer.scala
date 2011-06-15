package cook.config.runner.value.methods

import cook.config.runner.value.ValueMethod

object Installer {

  def install {
    // list
    ValueMethod.listMethods.put("join", Join)
    ValueMethod.listMethods.put("mkString", Join)
    ValueMethod.listMethods.put("get", Get)

    // string
    ValueMethod.stringMethods.put("contains", Contains)
    ValueMethod.stringMethods.put("split", Split)
    ValueMethod.stringMethods.put("startsWith", StartsWith)
    ValueMethod.stringMethods.put("endsWith", EndsWith)
  }
}
