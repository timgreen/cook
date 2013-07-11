package cook.app.version

import com.typesafe.config.ConfigFactory

object Version {

  lazy val version = {
    val config = ConfigFactory.load("version")
    config.getString("cook.version")
  }
}
