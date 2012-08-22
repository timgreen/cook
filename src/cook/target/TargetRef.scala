package cook.target

import cook.path.PathRef
import cook.path.PathUtil

import scala.tools.nsc.io.Path


class TargetRef(val name: String, val segments: List[String]) {

  def verify {
    name :: segments foreach { p =>
      assume(!p.contains(":"), "target name should not contains ':', %s".format(this))
      assume(p.nonEmpty, "target name & segments should not to empty, %s".format(this))
    }
    segments foreach { p =>
    }
  }
  verify

  lazy val buildDir: Path =
    segments.foldLeft(PathUtil().cookTargetBuildDir: Path)(_ / _) / ("COOK_TARGET_build_" + name)
  lazy val runDir: Path =
    buildDir.parent / ("COOK_TARGET_run_" + name)

  override def toString = "TargetRef(%s)".format(refName)
  lazy val refName = segments.mkString("/", "/", ":" + name)
}

object TargetRef {

  def apply(baseSegments: List[String], relative: String, name: String) = {
    val segments = PathRef.relative(baseSegments, relative)
    new TargetRef(name, segments)
  }
}
