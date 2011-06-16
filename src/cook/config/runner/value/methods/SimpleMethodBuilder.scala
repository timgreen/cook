package cook.config.runner.value.methods

import cook.config.runner.value.Value
import cook.config.runner.value.ValueMethod
import cook.config.runner.value.ValueMethodBuilder

class SimpleMethodBuilder(valueMethod: ValueMethod) extends ValueMethodBuilder {

  override def apply(value: Value) = valueMethod
}


object SimpleMethodBuilder {

  def apply(valueMethod: ValueMethod) = new SimpleMethodBuilder(valueMethod)
}
