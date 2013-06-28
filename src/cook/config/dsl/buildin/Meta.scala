package cook.config.dsl.buildin

import cook.meta.{ Meta => MMeta }
import cook.meta.MetaHelper

import scala.reflect.io.{ Path => SPath }

trait Meta {

  def filesToMeta(group: String, files: Seq[SPath]): MMeta = MetaHelper.buildFileMeta(group, files)
  def filesToMeta(group: String, file: SPath): MMeta = filesToMeta(group, file :: Nil)
  def stringToMeta(group: String, s: String): MMeta = {
    val m = new MMeta
    m.add(group, s.hashCode.toString, s.hashCode.toString)
    m
  }
}
