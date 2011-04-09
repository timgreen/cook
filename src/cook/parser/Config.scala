package cook.parser

import mouse.runtime.SourceString
import mouse.runtime.SourceFile

import java.io.File

import cook.parser.unit._

object Config {

  def parse(path: String, filename: String): BuildConfig = {
    val parser = new ConfigParser
    val isOk = parser.parse(new SourceFile(filename))
    if (isOk) {
      new BuildConfig(path, parser.semantics.getBuildConfig)
    } else {
      null
    }
  }
}
