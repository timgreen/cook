package cook.ref

trait Ref {

  def refName: String
}

trait RefFactory[R >: Ref] {

  def apply[R](refName: String): Option[R]
}
