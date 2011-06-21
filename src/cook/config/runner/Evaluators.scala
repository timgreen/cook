package cook.config.runner

import scala.annotation.tailrec
import scala.collection.immutable.VectorBuilder
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

import cook.config.parser.unit._
import cook.config.runner.ConfigType.{ ConfigType, COOK, cooki, COOK_ROOT }
import cook.config.runner.value._

case class CookReturn(value: Value) extends Throwable

object CookConfigEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, cookConfig: CookConfig): Value = {
    cookConfig.statements.foreach {
      StatementEvaluator.eval(configType, path, scope, _)
    }
    VoidValue("Void")
  }
}

// Statement

object StatementEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, statement: Statement): Value =
      statement match {
        case funcDef: FuncDef => FuncDefEvaluator.eval(configType, path, scope, funcDef)
        case assginment: Assginment => AssginmentEvaluator.eval(configType, path, scope, assginment)
        case ifStatement: IfStatement =>
          IfStatementEvaluator.eval(configType, path, scope, ifStatement)
        case forStatement: ForStatement =>
          ForStatementEvaluator.eval(configType, path, scope, forStatement)
        case exprStatement: ExprStatement =>
          ExprStatementEvaluator.eval(configType, path, scope, exprStatement)
        case returnStatement: ReturnStatement =>
          ReturnStatementEvaluator.eval(configType, path, scope, returnStatement)
        case _ => throw new EvalException(
            "this should never happen: unknown subclass of Statement: " + statement)
      }
}

object FuncDefEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, funcDef: FuncDef): Value = {
    if (scope.defineInCurrent(funcDef.name)) {
      throw new EvalException("Function \"%s\" already defined in current scope", funcDef.name)
    }
    if (scope.definedInParent(funcDef.name)) {
      // TODO(timgreen): log warning
    }
    // TODO(timgreen): error on override buildin

    val argsDef = ArgsEvaluator.eval(configType, path, scope, funcDef.argDefs)
    val functionValue = new FunctionValue(funcDef.name, path, scope, argsDef, funcDef.statements)

    scope(funcDef.name) = functionValue
    functionValue
  }
}

object AssginmentEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, assginment: Assginment): Value = {
    if (scope.definedInParent(assginment.id)) {
      // TODO(timgreen): log warning
    }
    // TODO(timgreen): error on override buildin

    val value = ExprEvaluator.eval(configType, path, scope, assginment.expr)
    value.name = assginment.id
    if (value.isVoid) {
      throw new EvalException("Can not assign void to %s", assginment.id)
    }

    scope(assginment.id) = value

    // NOTE(timgreen): return void for assginment
    VoidValue("Void")
  }
}

object IfStatementEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, ifStatement: IfStatement): Value = {
    val cond = ExprEvaluator.eval(configType, path, scope, ifStatement.cond).isTrue
    val block = if (cond) {
      ifStatement.trueBlock
    } else {
      ifStatement.falseBlock
    }

    BlockStatementsEvaluator.eval(configType, path, scope, block)
  }
}

object ForStatementEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, forStatement: ForStatement): Value = {
    val expr = ExprEvaluator.eval(configType, path, scope, forStatement.expr)
    val list = expr.toListValue("Need list value in for statement: %s", expr.name)
    val forScope = Scope(scope)
    for (v <- list) {
      forScope(forStatement.it) = v
      BlockStatementsEvaluator.eval(configType, path, forScope, forStatement.block)
    }

    VoidValue("for{}")
  }
}

object ExprStatementEvaluator {

  def eval(
      configType: ConfigType,
      path: String,
      scope: Scope,
      exprStatement: ExprStatement): Value = {
    ExprEvaluator.eval(configType, path, scope, exprStatement.expr)
  }
}

object ReturnStatementEvaluator {

  def eval(
      configType: ConfigType,
      path: String,
      scope: Scope,
      returnStatement: ReturnStatement): Value = {
    val returnValue = returnStatement.expr match {
      case Some(expr) => ExprEvaluator.eval(configType, path, scope, expr)
      case None => VoidValue("Void")
    }
    // NOTE(timgreen): use exception to deliver return value
    throw CookReturn(returnValue)
  }
}

// Expr

object ExprEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, expr: Expr): Value = {
    val it = expr.exprItems.iterator
    val v = ExprItemEvaluator.eval(configType, path, scope, it.next)
    expr.ops.foldLeft(v) {
      ValueOp.eval(_, _, ExprItemEvaluator.eval(configType, path, scope, it.next))
    }
  }
}

object ExprItemEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, exprItem: ExprItem): Value =
      exprItem match {
        case exprItemWithSuffix: ExprItemWithSuffix =>
          val v = SimpleExprItemEvaluator.eval(
              configType, path, scope, exprItemWithSuffix.simpleExprItem)
          exprItemWithSuffix.suffixs.foldLeft(v) {
            (v, suffix) => SuffixEvaluator.eval(configType, path, scope, v, suffix)
          }
        case exprItemWithUnary: ExprItemWithUnary =>
          val item = ExprItemEvaluator.eval(configType, path, scope, exprItemWithUnary.exprItem)
          item.unaryOp(exprItemWithUnary.unaryOp)
        case _ => throw new EvalException("this should never happen: unknown subclass of ExprItem")
      }
}

object ExprItemWithSuffixEvaluator {

  def eval(
      configType: ConfigType,
      path: String,
      scope: Scope,
      exprItemWithSuffix: ExprItemWithSuffix): Value = {
    val v = SimpleExprItemEvaluator.eval(configType, path, scope, exprItemWithSuffix.simpleExprItem)
    exprItemWithSuffix.suffixs.foldLeft(v) {
      (v, suffix) => SuffixEvaluator.eval(configType, path, scope, v, suffix)
    }
  }
}

object ExprItemWithUnaryEvaluator {

  def eval(
      configType: ConfigType,
      path: String,
      scope: Scope,
      exprItemWithUnary: ExprItemWithUnary): Value = {
    val item = ExprItemEvaluator.eval(configType, path, scope, exprItemWithUnary.exprItem)
    item.unaryOp(exprItemWithUnary.unaryOp)
  }
}

object SuffixEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, v: Value, suffix: Suffix): Value =
      suffix match {
        case idSuffix: IdSuffix => IdSuffixEvaluator.eval(configType, path, scope, v, idSuffix)
        case callSuffix: CallSuffix =>
          CallSuffixEvaluator.eval(configType, path, scope, v, callSuffix)
        case _ =>
          throw new EvalException("this should never happen: unknown subclass of Suffix")
      }
}

object IdSuffixEvaluator {

  // TODO(timgreen): support function value
  def eval(
      configType: ConfigType,
      path: String,
      scope: Scope,
      v: Value,
      idSuffix: IdSuffix): Value = {
    v.attr(idSuffix.id)
  }
}

object CallSuffixEvaluator {

  def eval(
      configType: ConfigType,
      path: String,
      scope: Scope,
      v: Value,
      callSuffix: CallSuffix): Value = {
    // Steps:
    // 1. check function def
    // 2. eval args into values, & wrapper into ArgList object
    // 3. call function in new scope
    // 4. return result

    if (!v.isInstanceOf[FunctionValue]) {
      throw new EvalException(
          "Value <%s>:%s is not function, can not be called", v.typeName, v.name)
    }
    val functionValue = v.asInstanceOf[FunctionValue]
    val args = ArgsValueEvaluator.eval(
        configType, path, scope, functionValue.name, functionValue.argsDef, callSuffix.args)

    FunctionValueCallEvaluator.eval(configType, path, args, functionValue)
  }
}

// SimpleExprItem

object SimpleExprItemEvaluator {

  def eval(
      configType: ConfigType,
      path: String,
      scope: Scope,
      simpleExprItem: SimpleExprItem): Value = {
    simpleExprItem match {
      case IntegerConstant(int) => NumberValue(int.toString, int)
      case StringLiteral(str) => StringValue("\"" + str + "\"", str)
      case CharLiteral(c) => CharValue("'" + c + "'", c)
      case identifier: Identifier => IdentifierEvaluator.eval(configType, path, scope, identifier)
      case lambdaDef: LambdaDef => LambdaDefEvaluator.eval(configType, path, scope, lambdaDef)
      case exprList: ExprList => ExprListEvaluator.eval(configType, path, scope, exprList)
      case listComprehensions: ListComprehensions =>
        ListComprehensionsEvaluator.eval(configType, path, scope, listComprehensions)
      case expr: Expr => ExprEvaluator.eval(configType, path, scope, expr)
      case map: Map => MapEvaluator.eval(configType, path, scope, map)
      case _ => throw new EvalException("this should never happen: unknown class of SimpleExprItem")
    }
  }
}

object IdentifierEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, identifier: Identifier): Value =
      scope.get(identifier.id) match {
        case Some(value) => value
        case None => throw new EvalException("var \"%s\" is not defined", identifier.id)
      }
}

object LambdaDefEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, lambdaDef: LambdaDef): Value = {
    val argsDef = ArgsEvaluator.eval(configType, path, scope, lambdaDef.argDefs)
    new FunctionValue("<lambda function>", path, scope, argsDef, lambdaDef.statements)
  }
}

object ExprListEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, exprList: ExprList): Value =
      ListValue("<list>", exprList.exprs.map {
        ExprEvaluator.eval(configType, path, scope, _)
      })
}

object ListComprehensionsEvaluator {

  def eval(
      configType: ConfigType,
      path: String,
      scope: Scope,
      listComprehensions: ListComprehensions): Value = {
    val listScope = Scope(scope)
    val list: Seq[Value] = scope.get(listComprehensions.list) match {
      case Some(ListValue(_, list)) => list
      // TODO(timgreen): name
      case _ => throw new EvalException("Need list value in ListComprehensions")
    }

    val it = listComprehensions.it
    val expr = listComprehensions.expr
    val resultBuilder = new VectorBuilder[Value]

    for (i <- list) {
      listScope(it) = i
      val cond = listComprehensions.cond match {
        case Some(cond) => ExprEvaluator.eval(configType, path, listScope, cond).isTrue
        case None => true
      }

      if (cond) {
        resultBuilder += ExprEvaluator.eval(configType, path, listScope, expr)
      }
    }

    ListValue("<generated list>", resultBuilder.result)
  }
}

object MapEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, map: Map): Value = {
    val mapValue = new HashMap[String, Value]
    for (keyValue <- map.keyValues) {
      if (mapValue.contains(keyValue.key)) {
        throw new EvalException("Found duplicated key \"%s\" in map value", keyValue.key)
      }
      mapValue(keyValue.key) = ExprEvaluator.eval(configType, path, scope, keyValue.expr)
    }
    MapValue("<map>", mapValue)
  }
}

// Arg

object ArgsEvaluator {

  def eval(configType: ConfigType, path: String, scope: Scope, argDefs: Seq[ArgDef]): ArgsDef = {
    checkArgs(argDefs)
    val defaultValues = new HashMap[String, Value]
    val names = argDefs.map {
      _ match {
        case ArgDefName(name) => name
        case ArgDefNameValue(name, expr) =>
          defaultValues(name) = ExprEvaluator.eval(configType, path, scope, expr)
          name
        case _ => throw new EvalException("this should never happen: unknown class of ArgDef")
      }
    }

    new ArgsDef(names, defaultValues)
  }

  private def checkArgs(args: Seq[ArgDef]) {
    try {
      checkArgsSeq(args)
    } catch {
      case e: ArgListException => throw new EvalException(
        "Default value should only appear at tail of arg def list: %s", args.map( _ match {
            case ArgDefName(name) => name
            case ArgDefNameValue(name, expr) => name + "=<default value>"
        }).mkString("[", ", ", "]"))
    }
  }

  @tailrec
  private def checkArgsSeq(args: Seq[ArgDef]) {
    args.headOption match {
      case Some(ArgDefName(_)) => checkArgsSeq(args.tail)
      case _ =>
        if (!args.forall { _.isInstanceOf[ArgDefNameValue] }) {
          throw new ArgListException
        }
    }
  }

  class ArgListException extends Throwable
}

object ArgsValueEvaluator {

  def eval(
      configType: ConfigType,
      path: String,
      scope: Scope,
      funcName: String,
      argsDef: ArgsDef,
      args: Seq[Arg]): Scope = {

    // 1. check wether args match argsDef
    // 2. create ArgsValue map

    checkArgs(argsDef, args, funcName)

    val argsValue = Scope(scope)
    argsValue.values ++= argsDef.defaultValues

    val names = new HashSet[String]

    for (i <- 0 until args.length) args(i) match {
      case ArgValue(expr) =>
        val value = ExprEvaluator.eval(configType, path, scope, expr)
        value.name = argsDef.names(i)
        argsValue(value.name) = value
      case ArgNamedValue(name, expr) => {
        if (names.contains(name)) {
          throw new EvalException(
              "dulpicated name \"%s\", in named-args func call \"%s\"",
              name,
              funcName)
        }
        names += name
        val value = ExprEvaluator.eval(configType, path, scope, expr)
        value.name = name
        argsValue(name) = value
      }
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

    argsValue
  }

  private def checkArgs(argsDef: ArgsDef, args: Seq[Arg], funcName: String) {
    if (args.length > argsDef.names.length) {
      throw new EvalException("Wrong arg number of function call \"%s\"", funcName)
    }

    try {
      checkArgsSeq(args)
    } catch {
      case e: ArgListException => throw new EvalException(
        "Named value should only appear at tail of arg list: %s", args.map ( _ match {
            case ArgValue(_) => "<value>"
            case ArgNamedValue(name, _) => name + "=<value>"
          }).mkString("[", ", ", "]"))
    }
  }

  @tailrec
  private def checkArgsSeq(args: Seq[Arg]) {
    args.headOption match {
      case Some(ArgValue(_)) => checkArgsSeq(args.tail)
      case _ =>
        if (!args.forall { _.isInstanceOf[ArgNamedValue] }) {
          throw new ArgListException
        }
    }
  }

  class ArgListException extends Throwable
}

// Others

object FunctionValueCallEvaluator {

  def eval(
      configType: ConfigType,
      path: String,
      argsValue: Scope,
      functionValue: FunctionValue): Value = {

    // TODO(timgreen): check config type
    // a. COOK_ROOT can only call function "include"
    // b. cooki can not call function "include"
    // c. COOK has no limitation
    functionValue match {
      case buildinFunction: BuildinFunction =>
        buildinFunction.eval(path, argsValue)
      case _ =>
        val result = try {
          BlockStatementsEvaluator.eval(configType, path, argsValue, functionValue.statements)
        } catch {
          case CookReturn(v) => v
          case e: EvalException =>
            throw new EvalException(e, "Got error when eval function \"%s\"", functionValue.name)
        }
        result.name = functionValue.name + "()"
        result
    }
  }
}

object BlockStatementsEvaluator {

  def eval(
      configType: ConfigType,
      path: String,
      scope: Scope,
      statements: Seq[Statement]): Value = {

    var lastValue: Value = VoidValue("")
    for (s <- statements) {
      lastValue = StatementEvaluator.eval(configType, path, scope, s)
    }

    lastValue
  }
}
