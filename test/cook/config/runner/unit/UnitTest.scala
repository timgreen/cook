package cook.config.runner.unit

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import cook.config.parser.unit._
import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.unit.RunnableUnitWrapper._
import cook.config.runner.value._

class UnitTest extends FlatSpec with ShouldMatchers {

  "Int" should "report error on unknown attr" in {
    evaluating {
      ExprItem(IntegerConstant(1), Seq[SelectorSuffix](IdSuffix("unknow"))).run("", Scope())
    } should produce [EvalException]
  }

  "String" should "have attr 'size'" in {
    val exprItem = ExprItem(StringLiteral("str"), Seq[SelectorSuffix](IdSuffix("size")))
    val result = exprItem.run("", Scope())
    result should be (NumberValue(3))
  }

  it should "have attr 'length'" in {
    val exprItem = ExprItem(StringLiteral("123456789"), Seq[SelectorSuffix](IdSuffix("length")))
    val result = exprItem.run("", Scope())
    result should be (NumberValue(9))
  }

  it should "report error on unknown attr" in {
    evaluating {
      ExprItem(StringLiteral("str"), Seq[SelectorSuffix](IdSuffix("unknow"))).run("", Scope())
    } should produce [EvalException]
  }

  "1 + 9" should "= 10" in {
    val intA = ExprItem(IntegerConstant(1), Seq[SelectorSuffix]())
    val intB = ExprItem(IntegerConstant(9), Seq[SelectorSuffix]())
    val expr = Expr(Seq(intA, intB), Seq("+"))
    val result = expr.run("", Scope())
    result should be (NumberValue(10))
  }

  "\"a\" + \"b\"" should "= \"ab\"" in {
    val strA = ExprItem(StringLiteral("a"), Seq[SelectorSuffix]())
    val strB = ExprItem(StringLiteral("b"), Seq[SelectorSuffix]())
    val expr = Expr(Seq(strA, strB), Seq("+"))
    val result = expr.run("", Scope())
    result should be (StringValue("ab"))
  }

  "\"a\" + 1" should "= \"a1\"" in {
    val strA = ExprItem(StringLiteral("a"), Seq[SelectorSuffix]())
    val strB = ExprItem(IntegerConstant(1), Seq[SelectorSuffix]())
    val expr = Expr(Seq(strA, strB), Seq("+"))
    val result = expr.run("", Scope())
    result should be (StringValue("a1"))
  }

  "List" should "have attr 'length'" in {
    val int1 = ExprItem(IntegerConstant(1), Seq[SelectorSuffix]())
    val int2 = ExprItem(IntegerConstant(2), Seq[SelectorSuffix]())
    val list = ExprList(Seq(Expr(Seq(int1), Seq[String]()), Expr(Seq(int2), Seq[String]())))
    val exprItem = ExprItem(list, Seq[SelectorSuffix](IdSuffix("length")))
    val result = exprItem.run("", Scope())
    result should be (NumberValue(2))
  }

  it should "have attr 'size'" in {
    val int1 = ExprItem(IntegerConstant(1), Seq[SelectorSuffix]())
    val int2 = ExprItem(IntegerConstant(2), Seq[SelectorSuffix]())
    val int3 = ExprItem(IntegerConstant(3), Seq[SelectorSuffix]())
    val list = ExprList(Seq(Expr(Seq(int1), Seq[String]()),
                            Expr(Seq(int2), Seq[String]()),
                            Expr(Seq(int3), Seq[String]())))
    val exprItem = ExprItem(list, Seq[SelectorSuffix](IdSuffix("size")))
    val result = exprItem.run("", Scope())
    result should be (NumberValue(3))
  }

  "[1, 2]" should "= [1, 2]" in {
    val int1 = ExprItem(IntegerConstant(1), Seq[SelectorSuffix]())
    val int2 = ExprItem(IntegerConstant(2), Seq[SelectorSuffix]())
    val list = ExprList(Seq(Expr(Seq(int1), Seq[String]()), Expr(Seq(int2), Seq[String]())))
    val exprItem = ExprItem(list, Seq[SelectorSuffix]())
    val result = exprItem.run("", Scope())
    result should be (ListValue(Seq[Value](NumberValue(1), NumberValue(2))))
  }

  "[1, 2] + 3" should "= [1, 2, 3]" in {
    val int1 = ExprItem(IntegerConstant(1), Seq[SelectorSuffix]())
    val int2 = ExprItem(IntegerConstant(2), Seq[SelectorSuffix]())
    val list = ExprList(Seq(Expr(Seq(int1), Seq[String]()), Expr(Seq(int2), Seq[String]())))
    val int3 = ExprItem(IntegerConstant(3), Seq[SelectorSuffix]())
    val expr = Expr(Seq(ExprItem(list, Seq[SelectorSuffix]()), int3), Seq("+"))
    val result = expr.run("", Scope())
    result should be (ListValue(Seq[Value](NumberValue(1), NumberValue(2), NumberValue(3))))
  }

  "[1, 2] ++ [3, 4]" should "= [1, 2, 3, 4]" in {
    val int1 = ExprItem(IntegerConstant(1), Seq[SelectorSuffix]())
    val int2 = ExprItem(IntegerConstant(2), Seq[SelectorSuffix]())
    val int3 = ExprItem(IntegerConstant(3), Seq[SelectorSuffix]())
    val int4 = ExprItem(IntegerConstant(4), Seq[SelectorSuffix]())
    val listA = ExprList(Seq(Expr(Seq(int1), Seq[String]()), Expr(Seq(int2), Seq[String]())))
    val listB = ExprList(Seq(Expr(Seq(int3), Seq[String]()), Expr(Seq(int4), Seq[String]())))
    val expr = Expr(Seq(ExprItem(listA, Seq[SelectorSuffix]()),
                        ExprItem(listB, Seq[SelectorSuffix]())), Seq("++"))
    val result = expr.run("", Scope())
    result should be (ListValue(Seq[Value](
        NumberValue(1), NumberValue(2), NumberValue(3), NumberValue(4))))
  }
}
