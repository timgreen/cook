package cook.parser

import cook.parser.unit._

/**
 * Build parse tree.
 */
class ConfigSemantics extends mouse.runtime.SemanticsBase {
  def getBuildConfig = config

  def buildConfig {
    val list =
        for (i <- 0 until rhsSize if rhs(i).isA("Command")) yield {
          rhs(i).get.asInstanceOf[Command]
        }
    config = list.toArray
  }

  def command {
    lhs.put(rhs(0).get)
  }

  def buildRule {
    val ruleName = rhs(0).get.asInstanceOf[String]
    val params = rhs(2).get.asInstanceOf[Map[String, Value]]
    lhs.put(new BuildRule(ruleName, params))
  }

  def ruleName {
    lhs.put(rhs(0).get)
  }

  def paramList: Boolean = {
    val map = scala.collection.mutable.HashMap.empty[String, Value]
    for (i <- 0 until rhsSize if rhs(i).isA("Param")) {
      val param = rhs(i).get.asInstanceOf[Param]
      if (map.contains(param.key)) {
        // key should be unique
        return false
      }
      map += (param.key -> param.value)
    }

    lhs.put(map.toMap)
    true
  }

  def param {
    val key = rhs(0).get.asInstanceOf[String]
    val value = rhs(2).get.asInstanceOf[Value]
    lhs.put(new Param(key, value))
  }

  def key {
    lhs.put(rhs(0).get)
  }

  def value {
    lhs.put(rhs(0).get)
  }

  def stringValue {
    val string = rhs(0).get.asInstanceOf[String]
    lhs.put(new StringValue(string))
  }

  def listValue {
    val list =
        for (i <- 0 until rhsSize if rhs(i).isA("StringValue")) yield {
          rhs(i).get.asInstanceOf[StringValue].value
        }
    lhs.put(new ListValue(list.toArray))
  }

  def stringLiteral {
    val chars = for (i <- 1 until (rhsSize - 2)) yield rhs(i).text
    lhs.put(chars.mkString)
  }

  def identifier {
    val chars = for (i <- 0 until (rhsSize - 1)) yield rhs(i).text
    lhs.put(chars.mkString)
  }

  private
  var config: Array[Command] = null
}
