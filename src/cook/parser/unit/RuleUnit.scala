package cook.parser.ruleunit

abstract class DefaultValue
case class StringValue(value: String) extends DefaultValue
case class ListStringValue(value: Array[String]) extends DefaultValue
case class NumberValue(value: Int) extends DefaultValue
case class ListNumberValue(value: Array[Int]) extends DefaultValue

class Param(val name: String, val typeName: String, val defaultValue: DefaultValue) {
  def isOptional = (defaultValue != null)
}

class RuleBlock  // TODO(timgreen)
class RuleRule(val ruleName: String, val params: Map[String, Param], val block: RuleBlock)

class RuleConfig(val path: String, val rules: Array[RuleRule])
