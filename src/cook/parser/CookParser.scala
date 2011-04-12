package cook.parser

import java.io.File
import scala.collection.mutable._

import mouse.runtime.SourceString
import mouse.runtime.SourceFile

import cook.parser.unit._

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
