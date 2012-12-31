package cook.ref

trait Ref {

  def refName: String

  def isFile = false
  def isDir = false
  def isTarget = false
  def isNativeTarget = false
  def isPluginTarget = false

  def as[R <: Ref]: R = this.asInstanceOf[R]
}

trait RefFactory[+R <: Ref] {

  def apply(baseSegments: List[String], refName: String): Option[R]
}
