package cook.config.runner.value.methods

import cook.config.runner.value.ValueMethod
import cook.config.runner.value.methods.list._
import cook.config.runner.value.methods.string._
import cook.config.runner.value.methods.target._

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

    // target
    ValueMethod.targetLabelMethodBuilders("deps") = DepsBuilder
    ValueMethod.targetLabelMethodBuilders("outputType") = OutputTypeBuilder
    ValueMethod.targetLabelMethodBuilders("setValues") = SetValuesBuilder
    ValueMethod.targetLabelMethodBuilders("value") = ValueBuilder
    ValueMethod.targetLabelMethodBuilders("values") = ValuesBuilder
  }
}
