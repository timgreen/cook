package cook.config.runner.value

import cook.config.runner.EvalException

abstract class Value(val typeName: String) {

  def attr(id: String): Value

  def isNull = false

  def ensureNotNull(): Value = ensureNotNull("Null is not allowed in expr")

  def ensureNotNull(errorMessage: String): Value = {
    if (isNull) {
      throw new EvalException(errorMessage)
    }
    this
  }

  def get(): Any
}

case class NullValue() extends Value("Null") {

  override def attr(id: String): Value = {
    throw new EvalException("Unsupportted attr \"%s\" on Null", id)
  }
  override def isNull = true
  override def get(): Any = null
}

case class NumberValue(int: Int) extends Value("Number") {

  override def attr(id: String): Value = id match {
    case _ => throw new EvalException("attr \"%s\" is not supportted by Number", id)
  }

  override def get(): Any = int
}

case class StringValue(str: String) extends Value("String") {

  override def attr(id: String): Value = id match {
    case "size" => NumberValue(str.size)
    case "length" => NumberValue(str.length)
    case _ => throw new EvalException("attr \"%s\" is not supportted by String", id)
  }

  override def get(): Any = str
}

case class ListValue(list: Seq[Value]) extends Value("List") {

  override def attr(id: String): Value = id match {
    case "size" => NumberValue(list.size)
    case "length" => NumberValue(list.length)
    case _ => throw new EvalException("attr \"%s\" is not supportted by List", id)
  }

  override def get(): Any = list
}

object ListValue {

  def apply(): ListValue = ListValue(Seq[Value]())
}
