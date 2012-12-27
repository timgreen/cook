package cook.ref

trait Ref {

  def refName: String
}

trait RefFactory[+R <: Ref] {

  def apply(baseSegments: List[String], refName: String): Option[R]
}
