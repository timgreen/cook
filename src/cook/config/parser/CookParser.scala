package cook.config.parser

import java.io.File

import cook.config.parser.runtime.SourceFile
import cook.config.parser.runtime.SourceString
import cook.config.parser.unit._
import cook.error.CookException

object CookParser {

  def parse(filename: String): CookConfig = parse(new File(filename))

  def parse(file: File): CookConfig = {
    val parser = new Parser
    if (!parser.parse(new SourceFile(file.getPath))) {
      // TODO(timgreen): wrapper parser error
      throw new ParserErrorException("Get error when parse: \"%s\"", file.getPath)
    }
    parser.semantics.getConfig
  }
}

class ParserErrorException(error: String, args: Any*) extends CookException(error, args: _*)
