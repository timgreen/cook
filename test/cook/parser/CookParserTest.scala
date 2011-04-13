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

  it should "be able to parse number values" in {
    val result = CookParser.parse("test6", "test/cook/parser/with_number_value.cook")

    val FuncCall(name, args) = result.statements(0)
    name should be ("testNumberValue")
    args.length should be (5)

    val ArgNamedValue(argName1, argExpr1) = args(1)
    val ArgNamedValue(argName2, argExpr2) = args(2)
    val ArgNamedValue(argName3, argExpr3) = args(3)
    val ArgNamedValue(argName4, argExpr4) = args(4)

    argName1 should be ("version")
    argExpr1.exprItems.length should be (1)
    argExpr1.exprItems(0).simpleExprItem should be (IntegerConstant(1))
    argExpr1.exprItems(0).selectorSuffixs.isEmpty should be (true)
    argExpr1.ops.isEmpty should be (true)

    argName2 should be ("oct")
    argExpr2.exprItems.length should be (1)
    argExpr2.exprItems(0).simpleExprItem should be (IntegerConstant(0123))
    argExpr2.exprItems(0).selectorSuffixs.isEmpty should be (true)
    argExpr2.ops.isEmpty should be (true)

    argName3 should be ("hex")
    argExpr3.exprItems.length should be (1)
    argExpr3.exprItems(0).simpleExprItem should be (IntegerConstant(0xA12AF))
    argExpr3.exprItems(0).selectorSuffixs.isEmpty should be (true)
    argExpr3.ops.isEmpty should be (true)

    argName4 should be ("list")
    argExpr4.exprItems.length should be (1)
    argExpr4.exprItems(0).selectorSuffixs.isEmpty should be (true)
    argExpr4.ops.isEmpty should be (true)

    val ExprList(exprs) = argExpr4.exprItems(0).simpleExprItem
    exprs.length should be (3)
    exprs(0).exprItems(0).simpleExprItem should be (IntegerConstant(13))
    exprs(1).exprItems(0).simpleExprItem should be (IntegerConstant(011))
    exprs(2).exprItems(0).simpleExprItem should be (IntegerConstant(0xAA))
  }

  it should "be able to detect errors" in {
    evaluating {
      CookParser.parse("test7", "test/cook/parser/error_miss_comma.cook")
    } should produce [ParserErrorException]
  }

  it should "report error on re-define build-in function" in {
    evaluating {
      CookParser.parse("test8", "test/cook/parser/error_redef_buildin_func.cook")
    } should produce [ParserErrorException]
  }
}
