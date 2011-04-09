package cook.parser.configunit

abstract class Value
case class StringValue(value: String) extends Value
case class ListStringValue(value: Array[String]) extends Value
case class NumberValue(value: Int) extends Value
case class ListNumberValue(value: Array[Int]) extends Value

class Param(val key: String, val value: Value)

abstract class Command
case class BuildRule(val ruleName: String, val params: Map[String, Value]) extends Command

class BuildConfig(val path: String, val commands: Array[Command])
