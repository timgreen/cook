package cook.config.dsl.buildin

import cook.config.dsl.ConfigContext
import cook.path.PathRef
import cook.target.TargetRef

import scala.tools.nsc.io.Path


object RefType extends Enumeration {
  type RefType = Value
  val Target, Path, Unknown = Value
}

trait Refs extends Error {

  def refs(refs: List[String])(implicit context: ConfigContext): (List[Path], List[TargetRef]) = {
    val groupedRefs = refs.groupBy(refType)
    if (groupedRefs.contains(RefType.Unknown)) {
      error("Unknow type refs: %s", groupedRefs(RefType.Unknown).mkString(", "))
    }
    (
      groupedRefs.getOrElse(RefType.Path, List()) map { r => pathRef(r)(context) },
      groupedRefs.getOrElse(RefType.Target, List()) map { r => targetRef(r)(context) }
    )
  }

  def refType(refString: String): RefType.RefType = {
    refString.count(_ == ':') match {
      case 0 => RefType.Path
      case 1 => RefType.Target
      case _ => RefType.Unknown
    }
  }
  def isTargetRef(refString: String) = (refType(refString) == RefType.Target)
  def isPathRef(refString: String) = (refType(refString) == RefType.Path)

  def pathRef(refString: String)(implicit context: ConfigContext): Path = {
    new PathRef(PathRef.relative(context.segments, refString)).p
  }

  def targetRef(refString: String)(implicit context: ConfigContext): TargetRef = {
    val Array(relative, name) = refString.split(":")
    TargetRef(context.segments, relative, name)
  }
}
