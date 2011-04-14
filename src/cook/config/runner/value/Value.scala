package cook.config.runner.value

abstract class Value
case class NumberValue(int: Int) extends Value
case class StringValue(str: String) extends Value
case class ListValue(list: Array[Value]) extends Value
