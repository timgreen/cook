package cook.config.runner.value.methods

import cook.config.runner.value.ValueMethod

object Installer {

  def install {
    // list
    ValueMethod.listMethodBuilders("join") = JoinBuilder
    ValueMethod.listMethodBuilders("mkString") = JoinBuilder
    ValueMethod.listMethodBuilders("get") = GetBuilder

    // string
    ValueMethod.stringMethodBuilders("contains") = ContainsBuilder
    ValueMethod.stringMethodBuilders("split") = SplitBuilder
    ValueMethod.stringMethodBuilders("startsWith") = StartsWithBuilder
    ValueMethod.stringMethodBuilders("endsWith") = EndsWithBuilder
  }
}
