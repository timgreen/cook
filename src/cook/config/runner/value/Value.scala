package cook.config.runner.value

import scala.collection.Seq

import cook.config.runner.unit.EvalException

abstract class Value {
  def op(o: String, v: Value): Value
  def typeName(): String
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
}

case class ListValue(list: Seq[Value]) extends Value {

  override def typeName = "List"

  override def op(o: String, v: Value): Value = o match {
    case "+" => ListValue((list.genericBuilder += v).result)
    case "++" => {
      v match {
        case ListValue(l) => ListValue((list.genericBuilder ++= l).result)
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
}


