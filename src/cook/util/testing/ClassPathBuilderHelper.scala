package cook.util.testing

import cook.util.ClassPathBuilder


object ClassPathBuilderHelper {

  def reset(builder: ClassPathBuilder) {
    builder.cp.clear
    builder.cpSet.clear
  }
}
