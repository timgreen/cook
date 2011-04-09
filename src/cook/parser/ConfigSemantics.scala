package cook.parser

import cook.parser.unit._

/**
 * Build parse tree.
 */
class ConfigSemantics extends mouse.runtime.SemanticsBase {
  def getBuildConfig = config
  def getErrors = errors  // TODO(timgreen): own diagnostic output

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

  def listStringValue {
    val list =
        for (i <- 0 until rhsSize if rhs(i).isA("StringValue")) yield {
          rhs(i).get.asInstanceOf[StringValue].value
        }
    lhs.put(new ListStringValue(list.toArray))
  }

  def numberValue {
    val number = rhs(0).get.asInstanceOf[Int]
    lhs.put(new NumberValue(number))
  }

  def listNumberValue {
    val list =
        for (i <- 0 until rhsSize if rhs(i).isA("NumberValue")) yield {
          rhs(i).get.asInstanceOf[NumberValue].value
        }
    lhs.put(new ListNumberValue(list.toArray))
  }

  // Lexical elements

  def integerConstant {
    lhs.put(rhs(0).get)
  }

  def decimalConstant {
    lhs.put(Integer.parseInt(lhs.text, 10))
  }

  def octalConstant {
    lhs.put(Integer.parseInt(lhs.text, 8))
  }

  def hexConstant {
    lhs.put(Integer.parseInt(rhsText(1, rhsSize), 16))
  }

  def identifier {
    val chars = for (i <- 0 until (rhsSize - 1)) yield rhs(i).text
    lhs.put(chars.mkString)
  }

  def stringLiteral {
    val chars = for (i <- 1 until (rhsSize - 2)) yield rhs(i).text
    lhs.put(chars.mkString)
  }

  private
  var config: Array[Command] = null
  var errors: Array[String] = null
}
