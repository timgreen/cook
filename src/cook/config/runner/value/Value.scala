package cook.config.runner.value

import scala.collection.Seq

import cook.config.runner.EvalException

abstract class Value {

  def typeName(): String

  def op(o: String, v: Value): Value

  def attr(id: String): Value

  def isNull = false

  def sureNotNull(): Value = sureNotNull("Null is not allowed in expr")

  def sureNotNull(errorMessage: String): Value = {
    if (isNull) {
      throw new EvalException(errorMessage)
    }
    this
  }

  def get(): Any
}

case class NullValue() extends Value {

  override def typeName = "Null"
  override def op(o: String, v: Value): Value = {
    throw new EvalException("Unsupportted operator \"%s\" on Null".format(o))
  }
  override def attr(id: String): Value = {
    throw new EvalException("Unsupportted attr \"%s\" on Null".format(id))
  }
  override def isNull = true
  override def get(): Any = null
}

case class NumberValue(int: Int) extends Value {

  override def typeName = "Number"

  override def op(o: String, v: Value): Value = v match {
    case NumberValue(i) => {
      o match {
        case "+" => NumberValue(int + i)
        case "-" => NumberValue(int - i)
        case "*" => NumberValue(int * i)
        case "/" => NumberValue(int / i)
        case _ => {
          throw new EvalException("Unsupportted operator \"%s\" on (Number op Number)".format(o))
        }
      }
    }
    case _ => {
      throw new EvalException(
        "Operation \"%s\" on (Number op %s) is not supportted".format(o, v.typeName))
    }
  }

  override def attr(id: String): Value = id match {
    case _ => throw new EvalException("attr \"%s\" is not supportted by Number".format(id))
  }

  override def get(): Any = int
}

case class StringValue(str: String) extends Value {

  override def typeName = "String"

  override def op(o: String, v: Value): Value = o match {
    case "+" => {
      val s = v match {
        case StringValue(s) => s
        case NumberValue(i) => i.toString
        case _ => {
          throw new EvalException(
              "Unsupportted operator \"+\" on (String op %s)".format(v.typeName))
        }
      }
      StringValue(str + s)
    }
    case "%" => {
      val list = v match {
        case ListValue(l) => {
          l.map { _.get }
        }
        case _ => {
          throw new EvalException(
              "Unsupportted operator \"%\" on (String op %s)".format(v.typeName))
        }
      }
      StringValue(str.format(list: _*))
    }
    case _ => {
      throw new EvalException("Unsupportted operator \"%s\" on (String op String)".format(o))
    }
  }

  override def attr(id: String): Value = id match {
    case "size" => NumberValue(str.size)
    case "length" => NumberValue(str.length)
    case _ => throw new EvalException("attr \"%s\" is not supportted by String".format(id))
  }

  override def get(): Any = str
}

case class ListValue(list: Seq[Value]) extends Value {

  override def typeName = "List"

  override def op(o: String, v: Value): Value = o match {
    case "+" => ListValue(list :+ v)
    case "++" => {
      v match {
        case ListValue(l) => ListValue(list ++ l)
        case _ => {
          throw new EvalException(
              "Operation \"++\" on (List op %s) is not supportted".format(v.typeName))
        }
      }
    }
    case _ => {
          throw new EvalException("Unsupportted operator \"%s\" on (List op %s)".format(o, v))
    }
  }

  override def attr(id: String): Value = id match {
    case "size" => NumberValue(list.size)
    case "length" => NumberValue(list.length)
    case _ => throw new EvalException("attr \"%s\" is not supportted by List".format(id))
  }

  override def get(): Any = list
}

object ListValue {

  def apply(): ListValue = ListValue(Seq[Value]())
}
