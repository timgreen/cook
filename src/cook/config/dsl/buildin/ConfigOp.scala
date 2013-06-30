package cook.config.dsl.buildin

import cook.app.Config

import com.typesafe.config.{ConfigFactory, Config => HoconConfig}

trait ConfigOp {

  def config = Config.conf
  def configWithDefault(defaultConf: String) = {
    val default = ConfigFactory.parseString(defaultConf)
    Config.conf.withFallback(default).resolve
  }
}
