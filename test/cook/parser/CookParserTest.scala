package cook.parser

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import cook.parser.unit._

class CookParserTest extends FlatSpec with ShouldMatchers {

  "Cook parser" should "be able to parse self build rule" in {
    val result = CookParser.parse("test0", "src/cook/parser/COOK")
    result.path should be ("test0")
    result.statements.length should be (3)
  }

  it should "be able to parse genpeg rule" in {
    val result = CookParser.parse("test1", "src/cook/parser/genpeg.cooki")
    result.path should be ("test1")
    result.statements.length should be (1)
  }

  /*
  it should "be able to parse simple java libaray rule" in {
    val result = Config.parse("test1", "test/cook/parser/simple_javalib.cook")
    result.path should be ("test1")
    result.commands.length should be (1)
  }

  it should "be able to parse more than one java libaray rules" in {
    val result = Config.parse("test2", "test/cook/parser/simple_javalib2.cook")
    result.path should be ("test2")
    result.commands.length should be (3)
  }

  it should "be able to parse config files with comments" in {
    val result = Config.parse("test3", "test/cook/parser/with_comments.cook")
    result.commands.length should be (1)
  }

  it should "be able to parse config files with inline comments" in {
    val result = Config.parse("test4", "test/cook/parser/with_inline_comments.cook")
    result.commands.length should be (3)

    val buildRule = result.commands(2).asInstanceOf[BuildRule]
    buildRule.ruleName should be ("java_lib")
    buildRule.params.size should be (2)

    val nameValue = buildRule.params("name").asInstanceOf[StringValue]
    nameValue.value should be ("test3")
    val depsValue = buildRule.params("deps").asInstanceOf[ListStringValue]
    depsValue.value.size should be (2)
    depsValue.value(0) should be (":test1")
    depsValue.value(1) should be (":test2")
  }

  it should "be able to parse number values" in {
    val result = Config.parse("test4", "test/cook/parser/with_number_value.cook")

    val buildRule = result.commands(0).asInstanceOf[BuildRule]
    buildRule.params.size should be (5)

    val version = buildRule.params("version").asInstanceOf[NumberValue]
    version.value should be (1)
    val oct = buildRule.params("oct").asInstanceOf[NumberValue]
    oct.value should be (0123)
    val hex = buildRule.params("hex").asInstanceOf[NumberValue]
    hex.value should be (0xA12AF)
    val list = buildRule.params("list").asInstanceOf[ListNumberValue]
    list.value should be (Array(13, 011, 0xAA))
  }

  it should "be able to detect errors" in {
    evaluating {
      Config.parse("test5", "test/cook/parser/error_miss_comma.cook")
    } should produce [ConfigErrorException]
  }

  it should "return error for duplicated param key" in {
    evaluating {
      Config.parse("test6", "test/cook/parser/error_duplicated_param_key.cook")
    } should produce [ConfigErrorException]
  }
  */
}

