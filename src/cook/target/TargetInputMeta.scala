package cook.target


class TargetInputMeta(groups: List[TargetInputMeta.Group]) {

}

object TargetInputMeta {
  case class Group(name: String, items: List[Item])
  case class Item(name: String, checkSum: String)
}
