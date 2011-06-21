package cook.config.runner

import cook.util.CookBaseException

class EvalException(error: String, args: Any*) extends CookBaseException(error, args: _*) {

  def this(cause: Throwable, error: String, args: Any*) {
    this(error.format(args: _*) + "\n" + cause.getMessage)
  }
}
