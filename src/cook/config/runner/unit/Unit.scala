package cook.config.runner.unit

import scala.collection.Seq

import cook.config.parser.unit._
import cook.config.runner.EvalException
import cook.config.runner.Scope
import cook.config.runner.value._

import RunnableUnitWrapper._

abstract class RunnableUnit {

  def getOrError(v: Option[Value]): Value = v match {
    case Some(value) => value
    case None => {
      // TODO(timgreen): better error message
      throw new EvalException("None is not allowed in expr")
    }
  }
}

class RunnableCookConfig(val cookConfig: CookConfig) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = {
    cookConfig.statements.foreach(_.run(path, scope))
    None
  }
}

class RunnableStatement(val statement: Statement) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = statement match {
    case funcStatement: FuncStatement => funcStatement.run(path, scope)
    case funcDef: FuncDef => funcDef.run(path, scope)
    case _ => None
  }
}

class RunnableFuncStatement(val funcStatement: FuncStatement) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = funcStatement match {
    case assginment: Assginment => assginment.run(path, scope)
    case simpleExprItem: SimpleExprItem => simpleExprItem.run(path, scope)
    case _ => None
  }
}

class RunnableAssginment(val assginment: Assginment) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = {
    if (scope.definedInParent(assginment.id)) {
      // TODO(timgreen): log warning
    }

    assginment.expr.run(path, scope.newChildScope) match {
      case Some(value) => scope.vars.put(assginment.id, value)
      case None => {
        throw new EvalException(
            "Assginment error: can not assign None to \"%s\"".format(assginment.id))
      }
    }

    None
  }
}

class RunnableExprItem(val exprItem: ExprItem) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = {
    val v = getOrError(exprItem.simpleExprItem.run(path, scope))
    Some(exprItem.selectorSuffixs.foldLeft(v) {
      (v, selectorSuffix) => {
        getOrError(new RunnableSelectorSuffix(selectorSuffix, v).run(path, scope))
      }
    })
  }
}

class RunnableSimpleExprItem(val simpleExprItem: SimpleExprItem) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = simpleExprItem match {
    case integerConstant: IntegerConstant => integerConstant.run(path, scope)
    case stringLiteral: StringLiteral => stringLiteral.run(path, scope)
    case identifier: Identifier => identifier.run(path, scope)
    case funcCall: FuncCall => None  // TODO(timgreen): impl
    case exprList: ExprList => exprList.run(path, scope)
    case expr: Expr => expr.run(path, scope)
    case _ => None
  }
}

class RunnableIntegerConstant(val integerConstant: IntegerConstant) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] =
      Some(NumberValue(integerConstant.int))
}

class RunnableStringLiteral(val stringLiteral: StringLiteral) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] =
      Some(StringValue(stringLiteral.str))
}

class RunnableIdentifier(val identifier: Identifier) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = {
    scope.get(identifier.id) match {
      case Some(value) => return Some(value)
      case None => throw new EvalException("var \"%s\" is not defined".format(identifier.id))
    }
    None
  }
}

class RunnableFuncCall(val funcCall: FuncCall) extends RunnableUnit {

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
      case None => throw new EvalException("func \"%s\" is not defined".format(funcCall.name))
    }
  }
}

class RunnableExprList(val exprList: ExprList) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] =
      Some(ListValue(exprList.exprs.map {
        _.run(path, scope.newChildScope) match {
          case Some(value) => value
          case None => {
            // TODO(timgreen): better error message
            throw new EvalException("None is not allowed in Expr List")
          }
        }
      }))
}

class RunnableExpr(val expr: Expr) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = {
    val it = expr.exprItems.iterator
    val v = getOrError(it.next.run(path, scope.newChildScope))
    Some(expr.ops.foldLeft(v) {
      _.op(_, getOrError(it.next.run(path, scope.newChildScope)))
    })
  }

}

class RunnableSelectorSuffix(val selectorSuffix: SelectorSuffix, val v: Value)
    extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = selectorSuffix match {
    case is: IdSuffix => new RunnableIdSuffix(is, v).run(path, scope)
    case cs: CallSuffix => new RunnableCallSuffix(cs, v).run(path, scope)
  }
}

class RunnableIdSuffix(val idSuffix: IdSuffix, val v: Value) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = Some(v.attr(idSuffix.id))
}

class RunnableCallSuffix(val callSuffix: CallSuffix, val v: Value) extends RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = {
    // TODO(timgreen):
    None
  }
}

class RunnableFuncDef(val funcDef: FuncDef, val scope: Scope) extends RunnableUnit {

  def run(path: String, argList: ArgList): Option[Value] = {
  // TODO(timgreen):
    None
  }
}

class ArgList(args: Seq[Arg]) {
  // TODO(timgreen):
}

object RunnableUnitWrapper {

  implicit def toRunnableUnit(cookConfig: CookConfig) = new RunnableCookConfig(cookConfig)

  implicit def toRunnableUnit(statement: Statement) = new RunnableStatement(statement)

  implicit def toRunnableUnit(funcStatement: FuncStatement) =
      new RunnableFuncStatement(funcStatement)

  implicit def toRunnableUnit(assginment: Assginment) = new RunnableAssginment(assginment)

  implicit def toRunnableUnit(exprItem: ExprItem) = new RunnableExprItem(exprItem)

  implicit def toRunnableUnit(simpleExprItem: SimpleExprItem) =
      new RunnableSimpleExprItem(simpleExprItem)

  implicit def toRunnableUnit(integerConstant: IntegerConstant) =
      new RunnableIntegerConstant(integerConstant)

  implicit def toRunnableUnit(stringLiteral: StringLiteral) =
      new RunnableStringLiteral(stringLiteral)

  implicit def toRunnableUnit(identifier: Identifier) = new RunnableIdentifier(identifier)

  implicit def toRunnableUnit(funcCall: FuncCall) = new RunnableFuncCall(funcCall)

  implicit def toRunnableUnit(exprList: ExprList) = new RunnableExprList(exprList)

  implicit def toRunnableUnit(expr: Expr) = new RunnableExpr(expr)
}
