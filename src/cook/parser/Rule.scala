package cook.parser

import mouse.runtime.SourceString
import mouse.runtime.SourceFile

import java.io.File

import cook.parser.ruleunit._

object Rule {

  def parse(path: String, filename: String): RuleConfig = {
    val parser = new RuleParser
    val isOk = parser.parse(new SourceFile(filename))
    if (isOk) {
      new RuleConfig(path, parser.semantics.getRuleConfig)
    } else {
      null
    }
  }
}
