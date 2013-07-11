package cook.app.version

import cook.meta.Meta

import com.typesafe.config.ConfigFactory

object Version {

  lazy val version = {
    val config = ConfigFactory.load("version")
    config.getString("cook.version")
  }

  val metaKey = "cookVersion"

  def meta: Meta = {
    val m = new Meta()
    m.add("system", "cookVersion", version)
    m
  }
}
