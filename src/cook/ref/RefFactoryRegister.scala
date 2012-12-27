package cook.ref

object RefManagerRegsiter {

  def init {
    RefManager.factorys ++= List(
      DirRefFactory,
      FileRefFactory,
      TargetRefFactory
    )
  }
}
