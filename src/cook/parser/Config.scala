package cook.parser

import java.io.File
import scala.collection.mutable._

import mouse.runtime.SourceString
import mouse.runtime.SourceFile

import cook.parser.configunit._

object Config {

  def parse(path: String, filename: String): BuildConfig = {
    val parser = new ConfigParser
    if (!parser.parse(new SourceFile(filename))) {
      // TODO(timgreen):
      throw new ConfigErrorException(null)
    }
    new BuildConfig(path, parser.semantics.getBuildConfig)
  }

  def check(buildConfig: BuildConfig): BuildConfig = {
    val errorMessages = new LinkedList[String]

    for (command <- buildConfig.commands) command match {
      case BuildRule(ruleName, params) => {
        val errorPrefix = "Build \"%s\", ".format(ruleName)
        def addError(message: String, args: Any*) {
          errorMessages :+ (errorPrefix + message.format(args))
        }

        // check param names:
        // 1. must contain "name"
        //
        // note: param name unique check will be done in parse process
        if (!params.contains("name")) {
          addError("Must contains key \"name\"")
        }
      }
    }

    buildConfig
  }
}

class ConfigErrorException(val messages: Array[String]) extends RuntimeException
