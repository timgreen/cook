package cook.config.runner.buildin

import scala.collection.mutable.HashMap

import cook.config.runner.Scope
import cook.config.runner.unit._
import cook.config.runner.value._
import cook.util._

/**
 * Buildin function os.
 *
 * return os type string
 *
 * Example:
 *
 * os()
 */
object Os extends RunnableFuncDef("os", Scope.ROOT_SCOPE, OsArgsDef(), null, null) {

  override def run(path: String, argsValue: ArgsValue): Value = {
    val osName = sys.Prop.StringProp("os.name").get.toLowerCase;
    val name =
        if (osName.startsWith("linux")) {
          "linux";
        } else if (osName.startsWith("mac") || osName.startsWith("darwin")) {
          "mac";
        } else {
          "unknown"
        }

    StringValue(name)
  }
}

object OsArgsDef {

  def apply() = {
    val names = Seq[String]()
    val defaultValues = new HashMap[String, Value]
    defaultValues.put("file", StringValue(""))

    new ArgsDef(names, defaultValues)
  }
}
