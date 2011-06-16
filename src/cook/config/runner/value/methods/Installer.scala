package cook.config.runner.value.methods

import cook.config.runner.value.ValueMethod

object Installer {

  def install {
    // list
    ValueMethod.listMethodBuilders("join") = SimpleMethodBuilder(Join)
    ValueMethod.listMethodBuilders("mkString") = SimpleMethodBuilder(Join)
    ValueMethod.listMethodBuilders("get") = SimpleMethodBuilder(Get)

    // string
    ValueMethod.stringMethodBuilders("contains") = SimpleMethodBuilder(Contains)
    ValueMethod.stringMethodBuilders("split") = SimpleMethodBuilder(Split)
    ValueMethod.stringMethodBuilders("startsWith") = SimpleMethodBuilder(StartsWith)
    ValueMethod.stringMethodBuilders("endsWith") = SimpleMethodBuilder(EndsWith)

    // function value
    ValueMethod.functionValueMethodBuilders("call") = CallBuilder
  }
}
