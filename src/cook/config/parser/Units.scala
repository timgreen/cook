package cook.config.parser.unit

class CookConfig(val statements: Seq[Statement])

// Statement

abstract class Statement
case class FuncDef(name: String,
                   argDefs: Seq[ArgDef],
                   statements: Seq[Statement]) extends Statement
case class Assginment(id: String, expr: Expr) extends Statement
case class IfStatement(cond: Expr,
                       trueBlock: Seq[Statement],
                       falseBlock: Seq[Statement]) extends Statement
case class ExprStatement(expr: Expr) extends Statement
case class ReturnStatement(expr: Option[Expr]) extends Statement

// Expr

case class Expr(exprItems: Seq[ExprItem], ops: Seq[String]) extends SimpleExprItem
abstract class ExprItem
case class ExprItemWithSuffix(val simpleExprItem: SimpleExprItem,
                              val suffixs: Seq[Suffix]) extends ExprItem
case class ExprItemWithUnary(val unaryOp: String,
                             val exprItem: ExprItem) extends ExprItem

abstract class Suffix
case class IdSuffix(id: String) extends Suffix
case class CallSuffix(args: Seq[Arg]) extends Suffix

// SimpleExprItem

abstract class SimpleExprItem extends Statement
case class IntegerConstant(int: Int) extends SimpleExprItem
case class StringLiteral(str: String) extends SimpleExprItem
case class CharLiteral(c: Char) extends SimpleExprItem
case class Identifier(id: String) extends SimpleExprItem
case class LambdaDef(argDefs: Seq[ArgDef],
                     statements: Seq[Statement]) extends SimpleExprItem
case class ExprList(exprs: Seq[Expr]) extends SimpleExprItem
case class ListComprehensions(expr: Expr,
                              it: String,
                              list: String,
                              cond: Option[Expr]) extends SimpleExprItem

// Arg

abstract class Arg
case class ArgValue(expr: Expr) extends Arg
case class ArgNamedValue(name: String, expr: Expr) extends Arg

abstract class ArgDef
case class ArgDefName(name: String) extends ArgDef
case class ArgDefNameValue(name: String, expr: Expr) extends ArgDef

