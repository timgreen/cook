package cook.parser

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import cook.parser.ruleunit._

class RuleTest extends FlatSpec with ShouldMatchers {

  "Rule parser" should "be able to parse genpeg rule" in {
    val result = Rule.parse("test0", "src/cook/parser/genpeg.cooki")
    result.path should be ("test0")
    result.rules.length should be (1)
  }

}

