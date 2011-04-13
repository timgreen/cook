package cook.config.parser

import java.io.File

import mouse.runtime.SourceString
import mouse.runtime.SourceFile

import cook.config.parser.unit._

object CookParser {

  def parse(filename: String): CookConfig = parse(new File(filename))

  def parse(file: File): CookConfig = {
    val parser = new Parser
    if (!parser.parse(new SourceFile(file.getPath))) {
      // TODO(timgreen):
      throw new ParserErrorException(null)
    }
    parser.semantics.getConfig
  }
}

class ParserErrorException(errors: Array[String]) extends RuntimeException
