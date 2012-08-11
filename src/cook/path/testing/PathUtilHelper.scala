package cook.path.testing

import cook.path.PathUtil

import scala.tools.nsc.io.Directory

object PathUtilHelper {

  def changeCookRoot(dir: Directory) {
    PathUtil.instance = new PathUtil(Some(dir))
  }
}
