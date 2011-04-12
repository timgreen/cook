package cook.parser

import scala.collection.mutable.ArrayBuffer

import cook.parser.unit._

/**
 * Build parse tree for COOK config.
 */
class Semantics extends mouse.runtime.SemanticsBase {

  def getConfig = config
  def getErrors = errors  // TODO(timgreen): own diagnostic output

  def cookConfig {
    val statements =
        if (rhs(1).isA("Statements")) {
          rhs(1).get.asInstanceOf[Array[Statement]]
        } else {
          Array[Statement]()
        }
    config = new Config(statements)
  }

  def statements {
    val statements =
        for (i <- 0 until rhsSize if rhs(i).isA("Statement")) yield {
          rhs(i).get.asInstanceOf[Statement]
        }
    lhs.put(statements.toArray)
  }

  def statement {
    lhs.put(rhs(0).get)
  }

  def funcCall {
    val id   = rhs(0).get.asInstanceOf[String]
    val args = if (rhs(2).isA("ArgList")) rhs(2).get.asInstanceOf[Array[Arg]] else Array[Arg]()
    lhs.put(new FuncCall(id, args))
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
          rhs(i).text.substring(0, 1)
        }
    lhs.put(new Expr(exprItems.toArray, ops.toArray))
  }

  def exprItem {
    val simpleExprItem = rhs(0).get.asInstanceOf[SimpleExprItem]
    val selectorSuffixs =
        for (i <- 1 until rhsSize if rhs(i).isA("SelectorSuffix")) yield {
          rhs(i).get.asInstanceOf[SelectorSuffix]
        }
    lhs.put(new ExprItem(simpleExprItem, selectorSuffixs.toArray))
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
          FuncCall
        } else if (rhs(0).isA("LBRK")) {
          val exprList =
              if (rhs(1).isA("ExprList")) {
                rhs(1).get.asInstanceOf[Array[Expr]]
              } else {
                Array[Expr]()
              }
          new ExprList(exprList)
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
    lhs.put(exprs.toArray)
  }

  def argList {
    val args =
        for (i <- 0 until rhsSize if rhs(i).isA("Arg")) yield {
          rhs(i).get.asInstanceOf[Arg]
        }
    lhs.put(args.toArray)
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
    val args = rhs(3).get.asInstanceOf[Array[Arg]]

    var i = 6
    val statements = new ArrayBuffer[FuncStatement]
    while (rhs(i).isA("FuncStatement")) {
      statements += rhs(i).asInstanceOf[FuncStatement]
      i = i + 1
    }

    val returnStatement = rhs(i).get.asInstanceOf[ReturnStatement]

    lhs.put(new FuncDef(name, args, statements.toArray, returnStatement))
  }

  def funcStatement {
    lhs.put(rhs(0).get)
  }

  def returnStatement {
    val expr = rhs(1).get.asInstanceOf[Expr]
    lhs.put(new ReturnStatement(expr))
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
    val chars = for (i <- 1 until (rhsSize - 2)) yield rhs(i).text
    lhs.put(chars.mkString)
  }

  private
  var config: Config = null
  var errors: Array[String] = null
}
