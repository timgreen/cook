package cook.app

import cook.config.{ConfigRefInclude, IncludeDefine, IncludeAsDefine}
import cook.ref.{RefManager, FileRef}

import com.typesafe.config.{ConfigFactory, Config => HoconConfig}
import scala.collection.JavaConversions._

object Config {

  private var config: HoconConfig = _

  def setConf(config: HoconConfig) {
    this.config = config.withFallback(defaultConf).resolve
    // verify
    maxJobs
    rootIncludes
  }

  def conf = config

  var cols: Int = _

  var cliMaxJobs: Option[Int] = _
  lazy val maxJobs: Int = Math.max(1, cliMaxJobs getOrElse config.getInt("cook.max-jobs"))
  lazy val rootIncludes: List[ConfigRefInclude] = includeRules ::: includeAsRules

  private def defaultConf = ConfigFactory.parseString(s"""
    processors = ${sys.runtime.availableProcessors}
    cook.include-rules = []
    cook.include-as-rules = {}
    cook.max-jobs = ${sys.runtime.availableProcessors - 1}
  """)

  private def includeRules: List[IncludeDefine] = {
    config.getStringList("cook.include-rules") map { path =>
      IncludeDefine(RefManager(Nil, path).as[FileRef])
    } toList
  }

  private def includeAsRules: List[IncludeAsDefine] = {
    config.getObject("cook.include-as-rules").unwrapped map { case (name: String, path: Any) =>
      IncludeAsDefine(RefManager(Nil, path.asInstanceOf[String]).as[FileRef], name)
    } toList
  }
}
