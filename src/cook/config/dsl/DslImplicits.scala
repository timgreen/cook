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


  private def t2l(t: {def productIterator: Iterator[Any]}): List[String] =
    t.productIterator.toList.asInstanceOf[List[String]]
  implicit def stringToList(s: String): List[String] = List(s)
  implicit def tuple1ToList(t: Tuple1[String]): List[String] = t2l(t)
  implicit def tuple2ToList(t: Tuple2[String, String]): List[String] = t2l(t)
  implicit def tuple3ToList(t: Tuple3[String, String, String]): List[String] = t2l(t)
  implicit def tuple4ToList(t: Tuple4[String, String, String, String]): List[String] = t2l(t)
  implicit def tuple5ToList(t: Tuple5[String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple6ToList(t: Tuple6[String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple7ToList(t: Tuple7[String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple8ToList(t: Tuple8[String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple9ToList(t: Tuple9[String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple10ToList(t: Tuple10[String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple11ToList(t: Tuple11[String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple12ToList(t: Tuple12[String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple13ToList(t: Tuple13[String, String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple14ToList(t: Tuple14[String, String, String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple15ToList(t: Tuple15[String, String, String, String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple16ToList(t: Tuple16[String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple17ToList(t: Tuple17[String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple18ToList(t: Tuple18[String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple19ToList(t: Tuple19[String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple20ToList(t: Tuple20[String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple21ToList(t: Tuple21[String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
  implicit def tuple22ToList(t: Tuple22[String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String]): List[String] = t2l(t)
}
