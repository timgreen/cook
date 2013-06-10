package cook.meta

import scala.reflect.io.{ Path => SPath }


object MetaHelper {

  def buildFileMeta(group: String, paths: List[SPath]): Meta = {
    val m = new Meta
    paths foreach { p =>
      m.add(group, p.path, FileHash.getHash(p))
    }
    m
  }
}
