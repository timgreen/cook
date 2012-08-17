package cook.target


case class TargetRef(name: String, segments: List[String]) {

  def verify {
    name :: segments foreach { p =>
      assume(!p.contains(":"), "target name should not contains ':', %s".format(this))
    }
  }
  verify

  def relativeTargetRef(path: String): TargetRef = {
    // TODO(timgreen):
    null
  }
}
