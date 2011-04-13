package cook.parser

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import cook.parser.unit._

class CookParserTest extends FlatSpec with ShouldMatchers {

  "Cook parser" should "be able to parse self build rule" in {
    val result = CookParser.parse("test0", "test/cook/parser/COOK")
    result.path should be ("test0")
    result.statements.length should be (1)
  }

  it should "be able to parse genpeg rule" in {
    val result = CookParser.parse("test1", "src/cook/parser/genpeg.cooki")
    result.path should be ("test1")
    result.statements.length should be (1)
  }

  it should "be able to parse simple java libaray rule" in {
    val result = CookParser.parse("test2", "test/cook/parser/simple_javalib.cook")
    result.statements.length should be (1)
  }

  it should "be able to parse more than one java libaray rules" in {
    val result = CookParser.parse("test3", "test/cook/parser/simple_javalib2.cook")
    result.statements.length should be (3)
  }

  it should "be able to parse config files with comments" in {
    val result = CookParser.parse("test4", "test/cook/parser/with_comments.cook")
    result.statements.length should be (1)
  }

  it should "be able to parse config files with inline comments" in {
    val result = CookParser.parse("test5", "test/cook/parser/with_inline_comments.cook")
    result.statements.length should be (3)

    val FuncCall(name, args) = result.statements(2)
    name should be ("java_lib")
    args.length should be (2)

    val ArgNamedValue(argName1, argExpr1) = args(0)
    val ArgNamedValue(argName2, argExpr2) = args(1)

    argName1 should be ("name")
    argExpr1.exprItems.length should be (1)
    argExpr1.exprItems(0).simpleExprItem should be (StringLiteral("test3"))
    argExpr1.exprItems(0).selectorSuffixs.isEmpty should be (true)
    argExpr1.ops.isEmpty should be (true)

    argName2 should be ("deps")
    argExpr2.exprItems.length should be (1)
    argExpr2.ops.isEmpty should be (true)
    val ExprList(exprs) = argExpr2.exprItems(0).simpleExprItem
    exprs.length should be (2)
    exprs(0).exprItems(0).simpleExprItem should be (StringLiteral(":test1"))
    exprs(1).exprItems(0).simpleExprItem should be (StringLiteral(":test2"))
  }

  it should "be able to detect errors" in {
    evaluating {
      CookParser.parse("test5", "test/cook/parser/error_miss_comma.cook")
    } should produce [ParserErrorException]
  }
}
/*
  it should "be able to parse number values" in {
    val result = CookParser.parse("test4", "test/cook/parser/with_number_value.cook")

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

  */
