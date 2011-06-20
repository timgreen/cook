package cook.config.runner.buildin

import scala.collection.mutable.HashMap

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
object Os extends BuildinFunction("os", OsArgsDef()) {

  override def eval(path: String, argsValue: Scope): Value = {
    val osName = sys.Prop.StringProp("os.name").get.toLowerCase;
    val name =
        if (osName.startsWith("linux")) {
          "linux";
        } else if (osName.startsWith("mac") || osName.startsWith("darwin")) {
          "mac";
        } else {
          "unknown"
        }

    StringValue("os()", name)
  }
}

object OsArgsDef {

  def apply() = {
    val names = Seq[String]()
    val defaultValues = new HashMap[String, Value]

    new ArgsDef(names, defaultValues)
  }
}
