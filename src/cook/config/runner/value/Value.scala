package cook.config.runner.value

import scala.collection.Seq

abstract class Value {
  def op(o: String, v: Value): Value
}

case class NumberValue(int: Int) extends Value {

  override def op(o: String, v: Value): Value = {
    v
  }
}

case class StringValue(str: String) extends Value {

  override def op(o: String, v: Value): Value = {
    v
  }
}

case class ListValue(list: Seq[Value]) extends Value {

  override def op(o: String, v: Value): Value = {
    v
  }
}


