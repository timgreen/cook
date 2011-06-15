package cook.config.runner

import scala.collection.immutable.VectorBuilder
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

import cook.config.parser.unit._
import cook.config.runner.value._

object CookConfigEvaluator {

  def eval(path: String, scope: Scope, cookConfig: CookConfig): Value = {
    cookConfig.statements.foreach {
      StatementEvaluator.eval(path, scope, _)
    }
    NullValue()
  }
}

object StatementEvaluator {

  def eval(path: String, scope: Scope, statement: Statement): Value = statement match {
    case funcStatement: FuncStatement => FuncStatementEvaluator.eval(path, scope, funcStatement)
    case funcDef: FuncDef => FuncDefEvaluator.eval(path, scope, funcDef)
    case _ => throw new EvalException("this should never happen: unknown subclass of Statement")
  }
}

object FuncStatementEvaluator {

  def eval(path: String, scope: Scope, funcStatement: FuncStatement): Value = funcStatement match {
    case assginment: Assginment => AssginmentEvaluator.eval(path, scope, assginment)
    case simpleExprItem: SimpleExprItem => SimpleExprItemEvaluator.eval(path, scope, simpleExprItem)
    case ifStatement: IfStatement => IfStatementEvaluator.eval(path, scope, ifStatement)
    case _ => throw new EvalException("this should never happen: unknown subclass of FuncStatement")
  }
}

object AssginmentEvaluator {

  def eval(path: String, scope: Scope, assginment: Assginment): Value = {
    if (scope.definedInParent(assginment.id)) {
      // TODO(timgreen): log warning
    }

    scope(assginment.id) = ExprEvaluator.eval(path, scope, assginment.expr)

    // NOTE(timgreen): return null for assginment
    NullValue()
  }
}

object ExprItemEvaluator {

  def eval(path: String, scope: Scope, exprItem: ExprItem): Value = exprItem match {
    case exprItemWithSuffix: ExprItemWithSuffix =>
      val v = SimpleExprItemEvaluator.eval(path, scope, exprItemWithSuffix.simpleExprItem)
      exprItemWithSuffix.selectorSuffixs.foldLeft(v) {
        (v, selectorSuffix) => SelectorSuffixEvaluator.eval(path, scope, v, selectorSuffix)
      }
    case exprItemWithUnary: ExprItemWithUnary =>
      val item = ExprItemEvaluator.eval(path, scope, exprItemWithUnary.exprItem)
      item.unaryOp(exprItemWithUnary.unaryOp)
    case _ => throw new EvalException("this should never happen: unknown subclass of ExprItem")
  }
}

object SimpleExprItemEvaluator {

  def eval(path: String, scope: Scope, simpleExprItem: SimpleExprItem): Value =
      simpleExprItem match {
        case integerConstant: IntegerConstant => NumberValue(integerConstant.int)
        case stringLiteral: StringLiteral => StringValue(stringLiteral.str)
        case charLiteral: CharLiteral => CharValue(charLiteral.c)
        case identifier: Identifier => IdentifierEvaluator.eval(path, scope, identifier)
        case funcCall: FuncCall => FuncCallEvaluator.eval(path, scope, funcCall)
        case lambdaDef: LambdaDef => LambdaDefEvaluator.eval(path, scope, lambdaDef)
        case exprList: ExprList => ExprListEvaluator.eval(path, scope, exprList)
        case expr: Expr => ExprEvaluator.eval(path, scope, expr)
        case listComprehensions: ListComprehensions =>
          ListComprehensionsEvaluator.eval(path, scope, listComprehensions)
        case _ => throw new EvalException("this should never happen: unknown class of SimpleExprItem")
      }
}

object IdentifierEvaluator {

  def eval(path: String, scope: Scope, identifier: Identifier): Value =
      scope.get(identifier.id) match {
        case Some(value) => value
        case None => throw new EvalException("var \"%s\" is not defined", identifier.id)
      }
}

object FuncCallEvaluator {

  def eval(path: String, scope: Scope, funcCall: FuncCall): Value = {
    // Steps:
    // 1. check function def
    // 2. eval args into values, & wrapper into ArgList object
    // 3. call function in new scope
    // 4. return result

    val functionValue =
        scope.get(funcCall.name) match {
          case Some(functionValue) =>
            if (!functionValue.isInstanceOf[FunctionValue]) {
              throw new EvalException(
                  "value \"%s\" is not function, can not be called", funcCall.name)
            }
            functionValue.asInstanceOf[FunctionValue]
          case None => throw new EvalException("func \"%s\" is not defined", funcCall.name)
        }

    val args =
        ArgsValueEvaluator.eval(path, scope, funcCall.name, functionValue.argsDef, funcCall.args)
    FunctionValueEvaluator.eval(path, args, functionValue)
  }
}

object ExprListEvaluator {

  def eval(path: String, scope: Scope, exprList: ExprList): Value =
      ListValue(exprList.exprs.map {
        ExprEvaluator.eval(path, scope, _)
      })
}

object ExprEvaluator {

  def eval(path: String, scope: Scope, expr: Expr): Value = {
    val it = expr.exprItems.iterator
    val v = ExprItemEvaluator.eval(path, scope, it.next)
    expr.ops.foldLeft(v) {
      ValueOp.eval(_, _, ExprItemEvaluator.eval(path, scope, it.next))
    }
  }

}

object ListComprehensionsEvaluator {

  def eval(path: String, scope: Scope, listComprehensions: ListComprehensions): Value = {
    val listScope = Scope(scope)
    val list: Seq[Value] = scope.get(listComprehensions.list) match {
      case Some(ListValue(list)) => list
      case _ => throw new EvalException("Need list value in ListComprehensions")
    }

    val it = listComprehensions.it
    val expr = listComprehensions.expr
    val resultBuilder = new VectorBuilder[Value]

    for (i <- list) {
      listScope(it) = i
      val cond =
          listComprehensions.cond match {
            case Some(cond) => {
              ExprEvaluator.eval(path, listScope, cond) match {
                case BooleanValue(bool) => bool
                case _ => throw new EvalException("Need bool value in ListComprehensions condition")
              }
            }
            case None => true
          }

      if (cond) {
        resultBuilder += ExprEvaluator.eval(path, listScope, expr)
      }
    }

    ListValue(resultBuilder.result)
  }
}

object SelectorSuffixEvaluator {

  def eval(path: String, scope: Scope, v: Value, selectorSuffix: SelectorSuffix): Value =
      selectorSuffix match {
        case idSuffix: IdSuffix => v.attr(idSuffix.id)
        case cs: CallSuffix =>
          val methodDef = ValueMethod(v, cs.call.name)
          val args = ArgsValueEvaluator.eval(
              path, scope, cs.call.name, methodDef.argsDef, cs.call.args)
          methodDef.eval(path, args, v)
        case _ =>
          throw new EvalException("this should never happen: unknown subclass of SelectorSuffix")
      }
}

object FunctionValueEvaluator {

  def eval(path: String, argsValue: Scope, functionValue: FunctionValue): Value = functionValue match {
    case buildinFunction: BuildinFunction =>
      buildinFunction.eval(path, argsValue)
    case _ =>
      for (statement <- functionValue.statements) {
        StatementEvaluator.eval(path, argsValue, statement)
      }

      functionValue.returnStatement match {
        case Some(expr) => ExprEvaluator.eval(path, argsValue, expr)
        case None => NullValue()
      }
  }
}

object IfStatementEvaluator {

  def eval(path: String, scope: Scope, ifStatement: IfStatement): Value = {
    val block =
        ExprEvaluator.eval(path, scope, ifStatement.cond) match {
          case BooleanValue.TRUE => ifStatement.trueBlock
          case BooleanValue.FALSE => ifStatement.falseBlock
          case _ => throw new EvalException("need Boolean value in if cond")
        }

    block.foreach {
      StatementEvaluator.eval(path, scope, _)
    }

    NullValue()
  }
}

object ArgsEvaluator {

  def eval(path: String, scope: Scope, argDefs: Seq[ArgDef]): ArgsDef = {
    val defaultValues = new HashMap[String, Value]
    val names = argDefs.map {
      _ match {
        case ArgDefName(name) => name
        case ArgDefNameValue(name, expr) =>
          defaultValues(name) = ExprEvaluator.eval(path, scope, expr)
          name
        case _ => throw new EvalException("this should never happen: unknown class of ArgDef")
      }
    }

    new ArgsDef(names, defaultValues)
  }
}

object FuncDefEvaluator {

  def eval(path: String, scope: Scope, funcDef: FuncDef): Value = {
    if (scope.defineInCurrent(funcDef.name)) {
      throw new EvalException("Function \"%s\" already defined in current scope", funcDef.name)
    }
    if (scope.definedInParent(funcDef.name)) {
      // TODO(timgreen): log warning
    }

    val argsDef = ArgsEvaluator.eval(path, scope, funcDef.argDefs)
    val functionValue =
        new FunctionValue(path, scope, argsDef, funcDef.statements, funcDef.returnStatement)

    scope(funcDef.name) = functionValue
    functionValue
  }
}

object LambdaDefEvaluator {

  def eval(path: String, scope: Scope, lambdaDef: LambdaDef): Value = {
    val argsDef = ArgsEvaluator.eval(path, scope, lambdaDef.argDefs)
    new FunctionValue(path, scope, argsDef, lambdaDef.statements, Some(lambdaDef.returnStatement))
  }
}

object ArgsValueEvaluator {

  def eval(
      path: String,
      scope: Scope,
      funcName: String,
      argsDef: ArgsDef,
      args: Seq[Arg]): Scope = {

    // 1. check wether args match argsDef
    // 2. create ArgsValue map

    val argsValue = Scope(scope)
    val isNamedList =
        (args.length != argsDef.names.length) ||
        (!args.isEmpty && args.head.isInstanceOf[ArgNamedValue])

    if (isNamedList) {  // Option named list
      argsValue.values ++= argsDef.defaultValues
      val names = new HashSet[String]
      for (arg <- args) arg match {
        case ArgNamedValue(name, expr) => {
          if (names.contains(name)) {
            throw new EvalException(
                "dulpicated name \"%s\", in named-args func call \"%s\"",
                name,
                funcName)
          }
          names += name
          argsValue(name) = ExprEvaluator.eval(path, scope, expr)
        }
        case _ => throw new EvalException(
            "name is required in named-args func call \"%s\"", funcName)
      }

      val argsUnknown = names -- argsDef.names
      if (argsUnknown.nonEmpty) {
        throw new EvalException(
            "Found unknown arg(s) in func call \"%s\": %s",
            funcName,
            argsUnknown.mkString(", "))
      }

      val argsMissing = argsDef.names.toSet -- argsValue.values.keys
      if (argsMissing.nonEmpty) {
        throw new EvalException(
            "Miss required arg(s) in func call \"%s\": %s",
            funcName,
            argsMissing.mkString(", "))
      }

    } else {  // Full list
      if (args.length != argsDef.names.length) {
        throw new EvalException(
            "Wrong arg number in fulllist-args func call \"%s\", except %d but got %d",
            funcName,
            argsDef.names.length,
            args.length)
      }
      val nameIter = argsDef.names.iterator
      for (arg <- args) {
        val name = nameIter.next
        val expr = arg match {
          case ArgValue(expr) => expr
          case ArgNamedValue(argName, expr) => {
            if (argName != name) {
              throw new EvalException(
                  "Wrong order for arg name \"%s\", in fulllist-args func call \"%s\", " +
                  "should be \"%s\"",
                  argName,
                  funcName,
                  name)
            }
            expr
          }
        }
        argsValue(name) = ExprEvaluator.eval(path, scope, expr)
      }
    }

    argsValue
  }
}
