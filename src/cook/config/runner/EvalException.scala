package cook.config.runner

import cook.util.CookBaseException

class EvalException(error: String, args: Any*) extends CookBaseException(error, args)
