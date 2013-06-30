package cook.config.dsl.buildin

import cook.config.dsl.ConfigContext
import cook.ref.Ref
import cook.ref.RefManager

trait RefOp {

  def ref(s: String)(implicit c: ConfigContext): Ref = RefManager(c.dir.segments, s)
  def refs(strings: String*)(implicit c: ConfigContext): List[Ref] =
    strings map { s => ref(s)(c) } toList
  def refs(strings: List[String])(implicit c: ConfigContext): List[Ref] = refs(strings: _*)
}
