package cook.config.runner.unit

import scala.collection.Seq

import cook.config.parser.unit._
import cook.config.runner.Scope
import cook.config.runner.value._

import RunnableUnitWrapper._

class RunnableCookConfig(val cookConfig: CookConfig) {

  def run(path: String, scope: Scope): Option[Value] = {
    cookConfig.statements.foreach(_.run(path, scope))
    None
  }
}

class RunnableStatement(val statement: Statement) {

  def run(path: String, scope: Scope): Option[Value] = statement match {
    case funcStatement: FuncStatement => funcStatement.run(path, scope)
    case funcDef: FuncDef => funcDef.run(path, scope)
    case _ => None
  }
}

class RunnableFuncStatement(val funcStatement: FuncStatement) {

  def run(path: String, scope: Scope): Option[Value] = funcStatement match {
    case assginment: Assginment => assginment.run(path, scope)
    case simpleExprItem: SimpleExprItem => simpleExprItem.run(path, scope)
    case _ => None
  }
}

class RunnableAssginment(val assginment: Assginment) {

  def run(path: String, scope: Scope): Option[Value] = {
    if (scope.definedInParent(assginment.id)) {
      // TODO(timgreen): log warning
    }

    assginment.expr.run(path, scope.newChildScope) match {
      case Some(value) => scope.vars.put(assginment.id, value)
      case None => {
        throw new EvalConfigException(
            "Assginment error: can not assign None to \"%s\"".format(assginment.id))
      }
    }

    None
  }
}

class RunnableSimpleExprItem(val simpleExprItem: SimpleExprItem) {

  def run(path: String, scope: Scope): Option[Value] = simpleExprItem match {
    case integerConstant: IntegerConstant => integerConstant.run(path, scope)
    case stringLiteral: StringLiteral => stringLiteral.run(path, scope)
    case identifier: Identifier => identifier.run(path, scope)
    // TODO(timgreen): impl others
    case _ => None
  }
}

class RunnableIntegerConstant(val integerConstant: IntegerConstant) {

  def run(path: String, scope: Scope): Option[Value] =
      Some(NumberValue(integerConstant.int))
}

class RunnableStringLiteral(val stringLiteral: StringLiteral) {

  def run(path: String, scope: Scope): Option[Value] =
      Some(StringValue(stringLiteral.str))
}

class RunnableIdentifier(val identifier: Identifier) {

  def run(path: String, scope: Scope): Option[Value] = {
    scope.get(identifier.id) match {
      case Some(value) => return Some(value)
      case None => throw new EvalConfigException("var \"%s\" is not defined".format(identifier.id))
    }
    None
  }
}

class RunnableFuncCall(val funcCall: FuncCall) {

  def run(path: String, scope: Scope): Option[Value] = {
    // Steps:
    // 1. eval args into values, & wrapper into ArgList object
    // 2. check function def
    // 3. call function in new scope
    // 4. return result

    val args = new ArgList(funcCall.args)

    // TODO(timgreen): impl buildin

    scope.getFunc(funcCall.name) match {
      case Some(runnableFuncDef) => return runnableFuncDef.run(path, args)
      case None => throw new EvalConfigException("func \"%s\" is not defined".format(funcCall.name))
    }
  }
}

class RunnableExprList(val exprList: ExprList) {

  def run(path: String, scope: Scope): Option[Value] =
      Some(ListValue(exprList.exprs.map {
        _.run(path, scope.newChildScope) match {
          case Some(value) => value
          case None => {
            // TODO(timgreen): better error message
            throw new EvalConfigException("find None in Expr List")
          }
        }
      }))
}

class RunnableExpr(val expr: Expr) {

  def run(path: String, scope: Scope): Option[Value] = {
    // TODO(timgreen):
    None
  }

}

class RunnableFuncDef(val funcDef: FuncDef, val scope: Scope) {

  def run(path: String, argList: ArgList): Option[Value] = {
  // TODO(timgreen):
    None
  }
}

class ArgList(args: Seq[Arg]) {
  // TODO(timgreen):
}

class EvalConfigException(error: String) extends RuntimeException

object RunnableUnitWrapper {

  implicit def toRunnableUnit(cookConfig: CookConfig) = new RunnableCookConfig(cookConfig)

  implicit def toRunnableUnit(statement: Statement) = new RunnableStatement(statement)

  implicit def toRunnableUnit(assginment: Assginment) = new RunnableAssginment(assginment)

  implicit def toRunnableUnit(integerConstant: IntegerConstant) =
      new RunnableIntegerConstant(integerConstant)

  implicit def toRunnableUnit(stringLiteral: StringLiteral) =
      new RunnableStringLiteral(stringLiteral)

  implicit def toRunnableUnit(identifier: Identifier) = new RunnableIdentifier(identifier)

  implicit def toRunnableUnit(expr: Expr) = new RunnableExpr(expr)
}
