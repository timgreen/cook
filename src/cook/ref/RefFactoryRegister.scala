package cook.ref

object RefFactoryRegister {

  def init {
    RefManager.factorys ++= List(
      DirRefFactory,
      FileRefFactory,
      NativeTargetRefFactory
    )
  }
}
