package cook.config.parser

import java.io.File

import mouse.runtime.SourceString
import mouse.runtime.SourceFile

import cook.config.parser.unit._

object CookParser {

  def parse(path: String, filename: String): CookConfig = {
    val parser = new Parser
    if (!parser.parse(new SourceFile(filename))) {
      // TODO(timgreen):
      throw new ParserErrorException(null)
    }
    new CookConfig(path, parser.semantics.getConfig.statements)
  }
}

class ParserErrorException(errors: Array[String]) extends RuntimeException
