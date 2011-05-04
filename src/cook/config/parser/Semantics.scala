package cook.config.parser

import scala.collection.Seq
import scala.collection.immutable.VectorBuilder

import cook.config.parser.unit._

/**
 * Build parse tree for COOK config.
 */
class Semantics extends mouse.runtime.SemanticsBase {

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

  def statement {
    lhs.put(rhs(0).get)
  }

  def funcCall {
    val id   = rhs(0).get.asInstanceOf[String]
    val args =
        if (rhs(2).isA("ArgList")) {
          rhs(2).get.asInstanceOf[Seq[Arg]]
        } else {
          Seq[Arg]()
        }
    lhs.put(new FuncCall(id, args))
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

  def assginment {
    val id    = rhs(0).get.asInstanceOf[String]
    val value = rhs(2).get.asInstanceOf[Expr]
    lhs.put(new Assginment(id, value))
  }

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
    val simpleExprItem = rhs(0).get.asInstanceOf[SimpleExprItem]
    val selectorSuffixs =
        for (i <- 1 until rhsSize if rhs(i).isA("SelectorSuffix")) yield {
          rhs(i).get.asInstanceOf[SelectorSuffix]
        }
    lhs.put(new ExprItem(simpleExprItem, selectorSuffixs))
  }


  def simpleExprItem {
    val e =
        if (rhs(0).isA("IntegerConstant")) {
          val int = rhs(0).get.asInstanceOf[Int]
          new IntegerConstant(int)
        } else if (rhs(0).isA("StringLiteral")) {
          val str = rhs(0).get.asInstanceOf[String]
          new StringLiteral(str)
        } else if (rhs(0).isA("Identifier")) {
          val id = rhs(0).get.asInstanceOf[String]
          new Identifier(id)
        } else if (rhs(0).isA("FuncCall")) {
          val funcCall = rhs(0).get.asInstanceOf[FuncCall]
          funcCall
        } else if (rhs(0).isA("LBRK")) {
          val exprList =
              if (rhs(1).isA("ExprList")) {
                rhs(1).get.asInstanceOf[Seq[Expr]]
              } else {
                Seq[Expr]()
              }
          new ExprList(exprList)
        } else if (rhs(0).isA("ListComprehensions")) {
          val listComprehensions = rhs(0).get.asInstanceOf[ListComprehensions]
          listComprehensions
        } else if (rhs(1).isA("Expr")) {
          val expr = rhs(1).get.asInstanceOf[Expr]
          expr
        }
    lhs.put(e)
  }


  def exprList {
    val exprs =
        for (i <- 0 until rhsSize if rhs(i).isA("Expr")) yield {
          rhs(i).get.asInstanceOf[Expr]
        }
    lhs.put(exprs)
  }

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

  def selectorSuffix {
    val s =
        if (rhs(1).isA("Identifier")) {
          val id = rhs(1).get.asInstanceOf[String]
          new IdSuffix(id)
        } else {
          val call = rhs(1).get.asInstanceOf[FuncCall]
          new CallSuffix(call)
        }
    lhs.put(s)
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
    val statementsBuilder = new VectorBuilder[FuncStatement]
    while (!rhs(i).isA("ReturnStatement") && !rhs(i).isA("RWING")) {
      if (rhs(i).isA("FuncStatement")) {
        // TODO(timgreen): fix append
        statementsBuilder += rhs(i).get.asInstanceOf[FuncStatement]
      }
      i = i + 1
    }

    val returnStatement =
        if (rhs(i).isA("ReturnStatement")) {
          Some(rhs(i).get.asInstanceOf[Expr])
        } else {
          None
        }

    lhs.put(new FuncDef(name, argDefs, statementsBuilder.result, returnStatement))
  }

  def funcStatement {
    lhs.put(rhs(0).get)
  }

  def returnStatement {
    val expr = rhs(1).get.asInstanceOf[Expr]
    lhs.put(expr)
  }

  def ifStatement {
    val cond = rhs(2).get.asInstanceOf[Expr]
    val trueBlockBuilder = new VectorBuilder[FuncStatement]
    var i = 5
    while (!rhs(i).isA("RWING")) {
      trueBlockBuilder += rhs(i).get.asInstanceOf[FuncStatement]
      i = i + 1
    }
    val falseBlockBuilder = new VectorBuilder[FuncStatement]
    i = i + 3
    if (i < rhsSize) {
      while (!rhs(i).isA("RWING")) {
        falseBlockBuilder += rhs(i).get.asInstanceOf[FuncStatement]
        i = i + 1
      }
    }

    lhs.put(IfStatement(cond, trueBlockBuilder.result, falseBlockBuilder.result))
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

  def stringChar {
    val char =
        if (rhs(0).isA("Escape")) {
          rhs(0).get
        } else {
          rhs(0).text
        }
    lhs.put(char)
  }

  def escape {
    lhs.put(rhs(0).get)
  }

  def simpleEscape {
    var char =
        rhs(1).text match {
          case "\\" => "\\"
          case "'" => "'"
          case "\"" => "\""
          case "b" => "\b"
          case "f" => "\f"
          case "n" => "\n"
          case "r" => "\r"
          case "t" => "\t"
        }
    lhs.put(char)
  }

  private
  var config: CookConfig = null
  var errors: Seq[String] = null
}
