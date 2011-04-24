package cook.util

class CookBaseExceptioin(error: String, args: Any*) extends RuntimeException(error.format(args: _*))
