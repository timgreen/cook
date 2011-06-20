package cook.config.parser

import scala.collection.Seq
import scala.collection.immutable.VectorBuilder

import cook.config.parser.unit._
import cook.config.parser.runtime.SemanticsBase

/**
 * Build parse tree for COOK config.
 */
class Semantics extends SemanticsBase {

  def getConfig = config
  def getErrors = errors  // TODO(timgreen): own diagnostic output

  private[parser]
  def cookConfig {
    val statements =
        for (i <- 0 until rhsSize if rhs(i).isA("Statement")) yield {
          rhs(i).get.asInstanceOf[Statement]
        }
    config = new CookConfig(statements)
  }

  // Statement

  def statement {
    lhs.put(rhs(0).get)
  }

  def funcDef {
    val name = rhs(1).get.asInstanceOf[String]
    val argDefs =
        if (rhs(3).isA("ArgDefList")) {
          rhs(3).get.asInstanceOf[Seq[ArgDef]]
        } else {
          Seq[ArgDef]()
        }

    var i = 4
    val statementsBuilder = new VectorBuilder[Statement]
    while (!rhs(i).isA("RWING")) {
      // TODO(timgreen): fix append
      if (rhs(i).isA("Statement")) {
        statementsBuilder += rhs(i).get.asInstanceOf[Statement]
      }
      i = i + 1
    }

    lhs.put(new FuncDef(name, argDefs, statementsBuilder.result))
  }

  def assginment {
    val id   = rhs(0).get.asInstanceOf[String]
    val expr = rhs(2).get.asInstanceOf[Expr]
    lhs.put(new Assginment(id, expr))
  }

  def ifStatement {
    val cond = rhs(2).get.asInstanceOf[Expr]
    val trueBlockBuilder = new VectorBuilder[Statement]
    var i = 5
    while (!rhs(i).isA("RWING")) {
      trueBlockBuilder += rhs(i).get.asInstanceOf[Statement]
      i = i + 1
    }
    val falseBlockBuilder = new VectorBuilder[Statement]
    i = i + 3
    if (i < rhsSize) {
      while (!rhs(i).isA("RWING")) {
        falseBlockBuilder += rhs(i).get.asInstanceOf[Statement]
        i = i + 1
      }
    }

    lhs.put(IfStatement(cond, trueBlockBuilder.result, falseBlockBuilder.result))
  }

  def exprStatement {
    val expr = rhs(0).get.asInstanceOf[Expr]
    lhs.put(ExprStatement(expr))
  }

  def returnStatement {
    val expr = if (rhs(1).isA("Expr")) {
      Some(rhs(1).get.asInstanceOf[Expr])
    } else {
      None
    }
    lhs.put(ReturnStatement(expr))
  }

  // Expr

  def expr {
    val exprItems =
        for (i <- 0 until rhsSize if i % 2 == 0) yield {
          rhs(i).get.asInstanceOf[ExprItem]
        }
    val ops =
        for (i <- 1 until rhsSize if i % 2 == 1) yield {
          rhs(i).text.trim
        }
    lhs.put(new Expr(exprItems, ops))
  }

  def exprItem {
    lhs.put(rhs(0).get)
  }

  def exprItemWithSuffix {
    val simpleExprItem = rhs(0).get.asInstanceOf[SimpleExprItem]
    val suffixs =
        for (i <- 1 until rhsSize if rhs(i).isA("CallSuffix") || rhs(i).isA("IdSuffix")) yield {
          rhs(i).get.asInstanceOf[Suffix]
        }
    lhs.put(new ExprItemWithSuffix(simpleExprItem, suffixs))
  }

  def exprItemWithUnary {
    val unaryOp = rhs(0).text.trim
    val exprItem = rhs(1).get.asInstanceOf[ExprItem]
    lhs.put(new ExprItemWithUnary(unaryOp, exprItem))
  }

  def idSuffix {
    val id = rhs(1).get.asInstanceOf[String]
    lhs.put(IdSuffix(id))
  }

  def callSuffix {
    val args =
        if (rhs(1).isA("ArgList")) {
          rhs(1).get.asInstanceOf[Seq[Arg]]
        } else {
          Seq[Arg]()
        }
    lhs.put(CallSuffix(args))
  }

  // SimpleExprItem

  def simpleExprItem {
    val e =
        if (rhs(0).isA("IntegerConstant")) {
          val int = rhs(0).get.asInstanceOf[Int]
          new IntegerConstant(int)
        } else if (rhs(0).isA("StringLiteral")) {
          val str = rhs(0).get.asInstanceOf[String]
          new StringLiteral(str)
        } else if (rhs(0).isA("CharLiteral")) {
          val c = rhs(0).get.asInstanceOf[Char]
          new CharLiteral(c)
        } else if (rhs(0).isA("LambdaDef")) {
          val lambdaDef = rhs(0).get.asInstanceOf[LambdaDef]
          lambdaDef
        } else if (rhs(0).isA("Identifier")) {
          val id = rhs(0).get.asInstanceOf[String]
          new Identifier(id)
        } else if (rhs(0).isA("ListComprehensions")) {
          val listComprehensions = rhs(0).get.asInstanceOf[ListComprehensions]
          listComprehensions
        } else if (rhs(0).isA("LBRK")) {
          val exprList =
              if (rhs(1).isA("ExprList")) {
                rhs(1).get.asInstanceOf[Seq[Expr]]
              } else {
                Seq[Expr]()
              }
          new ExprList(exprList)
        } else if (rhs(0).isA("LPAR")) {
          val expr = rhs(1).get.asInstanceOf[Expr]
          expr
        }
    lhs.put(e)
  }

  def lambdaDef {
    val argDefs =
        if (rhs(2).isA("ArgDefList")) {
          rhs(2).get.asInstanceOf[Seq[ArgDef]]
        } else {
          Seq[ArgDef]()
        }

    var i = 3
    val statementsBuilder = new VectorBuilder[Statement]
    while (!rhs(i).isA("RWING")) {
      if (rhs(i).isA("Statement")) {
        // TODO(timgreen): fix append
        statementsBuilder += rhs(i).get.asInstanceOf[Statement]
      }
      i = i + 1
    }

    lhs.put(new LambdaDef(argDefs, statementsBuilder.result))
  }

  def listComprehensions {
    val expr = rhs(1).get.asInstanceOf[Expr]
    val it = rhs(3).get.asInstanceOf[String]
    val list = rhs(5).get.asInstanceOf[String]
    val cond =
        if (rhsSize > 7) {
          Some(rhs(7).get.asInstanceOf[Expr])
        } else {
          None
        }
    lhs.put(ListComprehensions(expr, it, list, cond))
  }

  def exprList {
    val exprs =
        for (i <- 0 until rhsSize if rhs(i).isA("Expr")) yield {
          rhs(i).get.asInstanceOf[Expr]
        }
    lhs.put(exprs)
  }

  // Arg

  def argList {
    val args =
        for (i <- 0 until rhsSize if rhs(i).isA("Arg")) yield {
          rhs(i).get.asInstanceOf[Arg]
        }
    lhs.put(args)
  }

  def arg {
    val a =
        if (rhs(0).isA("Expr")) {
          val value = rhs(0).get.asInstanceOf[Expr]
          new ArgValue(value)
        } else {
          val name  = rhs(0).get.asInstanceOf[String]
          val value = rhs(2).get.asInstanceOf[Expr]
          new ArgNamedValue(name, value)
        }
    lhs.put(a)
  }

  def argDefList {
    val argDefs =
        for (i <- 0 until rhsSize if rhs(i).isA("ArgDef")) yield {
          rhs(i).get.asInstanceOf[ArgDef]
        }
    lhs.put(argDefs)
  }

  def argDef {
    val name = rhs(0).get.asInstanceOf[String]
    val ad =
        if (rhsSize > 1) {
          val value = rhs(2).get.asInstanceOf[Expr]
          new ArgDefNameValue(name, value)
        } else {
          new ArgDefName(name)
        }
    lhs.put(ad)
  }

  // Lexical elements

  def integerConstant {
    lhs.put(rhs(0).get)
  }

  def decimalConstant {
    lhs.put(Integer.parseInt(lhs.text, 10))
  }

  def octalConstant {
    lhs.put(Integer.parseInt(lhs.text, 8))
  }

  def hexConstant {
    lhs.put(Integer.parseInt(rhsText(1, rhsSize), 16))
  }

  def identifier {
    val chars = for (i <- 0 until (rhsSize - 1)) yield rhs(i).text
    lhs.put(chars.mkString)
  }

  def stringLiteral {
    val chars = for (i <- 1 until (rhsSize - 2)) yield rhs(i).get
    lhs.put(chars.mkString)
  }

  def charLiteral {
    lhs.put(rhs(1).get)
  }

  def stringChar {
    val char =
        if (rhs(0).isA("Escape")) {
          rhs(0).get
        } else {
          rhs(0).charAt(0)
        }
    lhs.put(char)
  }

  def escape {
    lhs.put(rhs(0).get)
  }

  def simpleEscape {
    var char =
        rhs(1).text match {
          case "\\" => '\\'
          case "'" => '\''
          case "\"" => '"'
          case "b" => '\b'
          case "f" => '\f'
          case "n" => '\n'
          case "r" => '\r'
          case "t" => '\t';
        }
    lhs.put(char)
  }

  private
  var config: CookConfig = null
  var errors: Seq[String] = null
}
