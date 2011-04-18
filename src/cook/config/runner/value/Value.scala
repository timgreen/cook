package cook.config.runner.value

import scala.collection.Seq

import cook.config.runner.EvalException

abstract class Value {
  def typeName(): String
  def op(o: String, v: Value): Value
  def attr(id: String): Value
  def isNull = false
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
}

case class StringValue(str: String) extends Value {

  override def typeName = "String"

  override def op(o: String, v: Value): Value = v match {
    case StringValue(s) => {
      o match {
        case "+" => StringValue(str + s)
        case _ => {
          throw new EvalException("Unsupportted operator \"%s\" on (String op String)".format(o))
        }
      }
    }
    case NumberValue(i) => {
      o match {
        case "+" => StringValue(str + i)
        case _ => {
          throw new EvalException("Unsupportted operator \"%s\" on (String op Number)".format(o))
        }
      }
    }
    case _ => {
      throw new EvalException(
        "Operation \"%s\" on (String op %s) is not supportted".format(o, v.typeName))
    }
  }

  override def attr(id: String): Value = id match {
    case "size" => NumberValue(str.size)
    case "length" => NumberValue(str.length)
    case _ => throw new EvalException("attr \"%s\" is not supportted by String".format(id))
  }
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
}

object ListValue {

  def apply(): ListValue = ListValue(Seq[Value]())
}
