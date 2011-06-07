package cook.app.config

object Config {

  var columns = 80
  var parallel = sys.runtime.availableProcessors

  def setColumns(columnsStr: String) {
    columns = columnsStr.toInt
  }
}
