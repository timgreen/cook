package cook.config.runner.value

import scala.collection.Seq

abstract class Value
case class NumberValue(int: Int) extends Value
case class StringValue(str: String) extends Value
case class ListValue(list: Seq[Value]) extends Value
