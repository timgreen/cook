package cook.config.testing

import cook.config.ConfigRef

object ConfigRefTestHelper {

  def clearCache = ConfigRef.cache.clear
}
