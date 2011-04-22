package cook.config.runner

class EvalException(error: String, args: Any*) extends RuntimeException(error.format(args: _*))
