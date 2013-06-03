package cook.app

import cook.config.{ConfigRefInclude, IncludeDefine, IncludeAsDefine}
import cook.ref.{RefManager, FileRef}

import com.typesafe.config.{ Config => HonocConfig}
import scala.collection.JavaConversions._

object Config {

  private var config: HonocConfig = _

  def setConf(config: HonocConfig) {
    this.config = config
    // verify
    maxJobs
    rootIncludes
  }

  def conf = config

  var cols: Int = _

  var cliMaxJobs: Option[Int] = _
  lazy val maxJobs: Int = Math.max(1, cliMaxJobs getOrElse config.getInt("cook.maxJobs"))
  lazy val rootIncludes: List[ConfigRefInclude] = includeRules ::: includeAsRules

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
