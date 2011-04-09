package cook.parser

import cook.parser.unit._

/**
 * Build parse tree.
 */
class Semantics extends mouse.runtime.SemanticsBase {
  def getBuildConfig = config

  def buildConfig {
    val list =
        for (i <- 0 until rhsSize if rhs(i).get.isInstanceOf[Command]) yield {
          rhs(i).get.asInstanceOf[Command]
        }
    config = list.toArray
  }

  def buildRule {
    lhs.put(new BuileRule(rhs(0).text, rhs(2).get.asInstanceOf[Map[String, Value]]))
  }

  def paramList {
    val list =
        for (i <- 0 until rhsSize if rhs(i).get.isInstanceOf[Param]) yield {
          val param = rhs(i).get.asInstanceOf[Param]
          param.key -> param.value
        }
    val map = Map[String, Value]() ++ list
    lhs.put(map)
  }

  def param {
    val key = rhs(0).text
    val value = rhs(2).get.asInstanceOf[Value]
    lhs.put(new Param(key, value))
  }

  def stringValue {
    lhs.put(new StringValue(lhs.text))
  }

  def listValue {
    val list =
        for (i <- 0 until rhsSize if rhs(i).get.isInstanceOf[StringValue]) yield {
          rhs(i).get.asInstanceOf[StringValue].value
        }
    lhs.put(new ListValue(list.toArray))
  }

  private
  var config: Array[Command] = null
}
