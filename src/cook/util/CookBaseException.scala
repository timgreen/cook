package cook.util

class CookBaseException(error: String, args: Any*)
    extends RuntimeException(error.format(args: _*)) {

  def this(cause: Throwable, error: String, args: Any*) {
    this(error.format(args: _*) + "\n" + cause.getMessage)
  }
}
