package cook.target

import cook.util.CookBaseException

class TargetException(error: String, args: Any*) extends CookBaseException(error, args)
