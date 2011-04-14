package cook.config.parser.unit

import scala.collection.Seq

class CookConfig(val statements: Seq[Statement])

abstract class Statement

abstract class FuncStatement extends Statement
case class Assginment(id: String, expr: Expr) extends FuncStatement

case class ExprItem(val simpleExprItem: SimpleExprItem, val selectorSuffixs: Seq[SelectorSuffix])

abstract class SimpleExprItem extends FuncStatement
case class IntegerConstant(int: Int) extends SimpleExprItem
case class StringLiteral(str: String) extends SimpleExprItem
case class Identifier(id: String) extends SimpleExprItem
case class FuncCall(name: String, args: Seq[Arg]) extends SimpleExprItem
case class ExprList(exprs: Seq[Expr]) extends SimpleExprItem
case class Expr(exprItems: Seq[ExprItem], ops: Seq[String]) extends SimpleExprItem

abstract class Arg
case class ArgValue(expr: Expr) extends Arg
case class ArgNamedValue(name: String, expr: Expr) extends Arg

abstract class SelectorSuffix
case class IdSuffix(id: String) extends SelectorSuffix
case class CallSuffix(call: FuncCall) extends SelectorSuffix

case class FuncDef(name: String,
                   args: Seq[Arg],
                   statements: Seq[FuncStatement],
                   returnStatement: ReturnStatement) extends Statement

case class ReturnStatement(val expr: Expr)
