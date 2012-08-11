package cook.path.testing

import cook.path.PathUtil

import scala.tools.nsc.io.Directory

object PathUtilHelper {

  def rakePath(
    cookRootOption: Option[Directory] = None,
    cookBuildDirOption: Option[Directory] = None,
    cookConfigScalaSourceDirOption: Option[Directory] = None,
    cookConfigClassDirOption: Option[Directory] = None,
    cookTargetBuildDirOption: Option[Directory] = None,
    cookConfigMetaDirOption: Option[Directory] = None) = {

    val p = new PathUtil(
      cookRootOption,
      cookBuildDirOption,
      cookConfigScalaSourceDirOption,
      cookConfigClassDirOption,
      cookTargetBuildDirOption,
      cookConfigMetaDirOption
    )
    PathUtil.instance = p
    p
  }
}
