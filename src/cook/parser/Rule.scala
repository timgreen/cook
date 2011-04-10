package cook.parser

import java.io.File
import scala.collection.mutable._

import mouse.runtime.SourceString
import mouse.runtime.SourceFile

import cook.parser.ruleunit._

object Rule {

  def parse(path: String, filename: String): RuleConfig = {
    val parser = new RuleParser
    val isOk = parser.parse(new SourceFile(filename))
    if (!isOk) {
      // TODO(timgreen):
      throw new RuleErrorException(null)
    }
    new RuleConfig(path, parser.semantics.getRuleConfig)
  }

  def check(ruleConfig: RuleConfig): RuleConfig = {
    val errorMessages = new LinkedList[String]

    for (rule <- ruleConfig.rules) {
      val errorPrefix = "Rule \"%s\", ".format(rule.ruleName)
      def addError(message: String, args: Any*) {
        errorMessages :+ (errorPrefix + message.format(args))
      }

      // check param names:
      // 1. should not contain Implicit Params: "name", "deps"
      // 2. default value should match param type
      //
      // note: param name unique check will be done in parse process
      val nameSet = new HashSet[String]
      for (p <- rule.params.values) {
        if (implicitParams.contains(p.name)) {
          addError("Can not use implicit param name: \"%s\"", p.name)
        }
        if (nameSet.contains(p.name)) {
          addError("Duplicated param name: \"%s\"", p.name)
        }
        if (classToType(p.defaultValue.getClass) != p.typeName) {
          addError("Param default value type mismatch: except \"%s\" but got \"%s\"",
              p.typeName,
              classToType(p.defaultValue.getClass))
        }
        nameSet += p.name
      }
    }

    if (!errorMessages.isEmpty) {
      throw new RuleErrorException(errorMessages.toArray)
    }
    ruleConfig
  }

  val implicitParams = Set("name", "deps")
  val classToType: Map[Any, String] = Map(
      classOf[StringValue]     -> "string"    ,
      classOf[ListStringValue] -> "stringlist",
      classOf[NumberValue]     -> "int"       ,
      classOf[ListNumberValue] -> "intlist"
  )
}

class RuleErrorException(val messages: Array[String]) extends RuntimeException
