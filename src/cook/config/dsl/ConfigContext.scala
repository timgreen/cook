package cook.config.dsl

import cook.config.ConfigRef
import cook.path.PathRef

class ConfigContext(val ref: ConfigRef) {

  def path = ref.parentPath

}
