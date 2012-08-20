package cook.config.dsl.buildin

import cook.error.CookException


trait Error {

  def error(msg: String, params: Any*) = {
    throw new CookException(msg, params: _*)
  }
}
