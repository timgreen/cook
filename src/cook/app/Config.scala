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
    rootIncludes
  }

  def conf = config
  def quiet: Boolean = config.getBoolean("cook.quiet")

  var cols: Int = _

  lazy val rootIncludes: List[ConfigRefInclude] = includeRules ::: includeAsRules

  private def defaultConf = ConfigFactory.parseString(s"""
    processors = ${sys.runtime.availableProcessors}
    cook {
      include-rules = []
      include-as-rules = {}
      akka.actor.typed.timeout = 100 days
      worker-dispatcher {
        type = "Dispatcher"
        executor = "fork-join-executor"
        fork-join-executor {
          parallelism-min = 1
          parallelism-factor = 1.0
        }
      }
      quiet = false
    }
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
