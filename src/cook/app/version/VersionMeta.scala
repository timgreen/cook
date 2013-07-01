package cook.app.version

import cook.meta.Meta

object VersionMeta {

  val key = "cookVersion"

  def apply(): Meta = {
    val m = new Meta()
    m.add("system", "cookVersion", Version.version)
    m
  }
}
