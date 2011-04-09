package cook.parser.ruleunit

abstract class DefaultValue
case class StringValue(value: String) extends DefaultValue
case class ListStringValue(value: Array[String]) extends DefaultValue
case class NumberValue(value: Int) extends DefaultValue
case class ListNumberValue(value: Array[Int]) extends DefaultValue

class Param(val key: String, val value: DefaultValue)

abstract class Command
case class RuleRule(val ruleName: String, val params: Map[String, DefaultValue]) extends Command

class RuleConfig(val path: String, val rules: Array[RuleRule])
