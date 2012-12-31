package cook.error

class CookException(val e: CookException, error: String, args: Any*)
  extends RuntimeException(error.format(args: _*)) {

  def this(error: String, args: Any*) {
    this(null: CookException, error, args: _*)
  }

  override def toString(): String = toString("")
  def toString(indent: String): String = {
    val s = getMessage.split("\n") map { indent + _ } mkString("\n")
    e match {
      case null => s
      case _ => s + "\n" + e.toString(indent + "  ")
    }
  }
}
