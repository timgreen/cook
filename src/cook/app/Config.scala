package cook.app

import com.typesafe.config.{ Config => HonocConfig}

import scala.collection.JavaConversions._

object Config {

  private var config: HonocConfig = _

  def setConf(config: HonocConfig) {
    this.config = config
    // verify
    maxJobs
    mixinRules
    importRules
  }

  def conf = config

  var cols: Int = _

  var cliMaxJobs: Option[Int] = _
  lazy val maxJobs: Int = Math.max(1, cliMaxJobs getOrElse config.getInt("cook.maxJobs"))
  lazy val mixinRules: List[String] = config.getStringList("cook.mixin-rules") toList
  lazy val importRules: Map[String, String] = buildImportRules

  private def buildImportRules = {
    config.getObject("cook.import-rules").unwrapped map { case (k: String, v: Any) =>
      k -> v.asInstanceOf[String]
    } toMap
  }
}
