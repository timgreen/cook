package cook.util

class CookBaseException(error: String, args: Any*) extends RuntimeException(error.format(args: _*))
