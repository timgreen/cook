package cook.error

object ErrorTracking {

  def wrapperError[T](error: String, args: Any*)(op: => T): T = {
    try {
      op
    } catch {
      case e: CookException =>
        throw new CookException(e, error, args: _*)
    }
  }

  def reportError(error: String, args: Any*) = {
    throw new CookException(error, args: _*)
  }
}
