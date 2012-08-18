package cook.config.dsl

import cook.path.PathRef
import cook.target.TargetRef


trait DslImplicits {

  implicit def string2targetRef(s: String)(implicit context: ConfigContext): TargetRef = {
    val Array(ref, name) = s.split(":", 2)
    new TargetRef(name, PathRef.relative(context.segments, ref))
  }
  implicit def stringList2targetRefList(
    list: List[String])(implicit context: ConfigContext): List[TargetRef] = {
    list map { s => string2targetRef(s)(context) }
  }
}
