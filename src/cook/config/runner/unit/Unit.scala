package cook.config.runner.unit

import cook.config.parser.unit._
import cook.config.runner.Scope
import cook.config.runner.value._

trait RunnableUnit {

  def run(path: String, scope: Scope): Option[Value] = None
}

import RunnableUnitWrapper._

class RunnableCookConfig(cookConfig: CookConfig) extends Proxy with RunnableUnit {
  // Proxy.self
  def self: Any = cookConfig

  override def run(path: String, scope: Scope): Option[Value] = {
    cookConfig.statements.foreach(_.run(path, scope))
    None
  }
}

class RunnableStatement(statement: Statement) extends Proxy with RunnableUnit {
  // Proxy.self
  def self: Any = statement

  override def run(path: String, scope: Scope): Option[Value] = statement match {
    case funcStatement: FuncStatement => funcStatement.run(path, scope)
    case funcDef: FuncDef => funcDef.run(path, scope)
    case _ => None
  }
}

class RunnableFuncStatement(funcStatement: FuncStatement) extends Proxy with RunnableUnit {
  // Proxy.self
  def self: Any = funcStatement

  override def run(path: String, scope: Scope): Option[Value] = funcStatement match {
    case assginment: Assginment => assginment.run(path, scope)
    case simpleExprItem: SimpleExprItem => simpleExprItem.run(path, scope)
    case _ => None
  }
}

class RunnableAssginment(assginment: Assginment) extends Proxy with RunnableUnit {
  // Proxy.self
  def self: Any = assginment

  override def run(path: String, scope: Scope): Option[Value] = {
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

class RunnableSimpleExprItem(simpleExprItem: SimpleExprItem) extends Proxy with RunnableUnit {
  // Proxy.self
  def self: Any = simpleExprItem

  override def run(path: String, scope: Scope): Option[Value] = simpleExprItem match {
    case integerConstant: IntegerConstant => integerConstant.run(path, scope)
    case stringLiteral: StringLiteral => stringLiteral.run(path, scope)
    case identifier: Identifier => identifier.run(path, scope)
    // TODO(timgreen): impl others
    case _ => None
  }
}

class RunnableIntegerConstant(integerConstant: IntegerConstant) extends Proxy with RunnableUnit {
  // Proxy.self
  def self: Any = integerConstant

  override def run(path: String, scope: Scope): Option[Value] =
      Some(NumberValue(integerConstant.int))
}

class RunnableStringLiteral(stringLiteral: StringLiteral) extends Proxy with RunnableUnit {
  // Proxy.self
  def self: Any = stringLiteral

  override def run(path: String, scope: Scope): Option[Value] =
      Some(StringValue(stringLiteral.str))
}

class RunnableIdentifier(identifier: Identifier) extends Proxy with RunnableUnit {
  // Proxy.self
  def self: Any = identifier

  override def run(path: String, scope: Scope): Option[Value] = {
    scope.get(identifier.id) match {
      case Some(value) => return Some(value)
      case None => throw new EvalConfigException("var \"%s\" not defined".format(identifier.id))
    }
    None
  }
}

class RunnableExpr(expr: Expr) extends Proxy with RunnableUnit {
  // Proxy.self
  def self: Any = expr

  override def run(path: String, scope: Scope): Option[Value] = {

    // TODO(timgreen):
    None
  }

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
