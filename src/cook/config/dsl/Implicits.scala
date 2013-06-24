package cook.config.dsl

import cook.ref.Ref
import cook.ref.RefManager

trait Implicits {

  def ref(s: String)(implicit c: ConfigContext): Ref = RefManager(c.dir.segments, s)
  def refs(strings: String*)(implicit c: ConfigContext): List[Ref] =
    strings map { s => ref(s)(c) } toList
  def refs(strings: List[String])(implicit c: ConfigContext): List[Ref] = refs(strings: _*)
  implicit def string2ref(s: String)(implicit c: ConfigContext): Ref = ref(s)(c)
  implicit def string2refs(s: String)(implicit c: ConfigContext): List[Ref] = List(ref(s)(c))
  implicit def strings2refs(strings: Seq[String])(implicit c: ConfigContext): List[Ref] =
    refs(strings: _*)
}
