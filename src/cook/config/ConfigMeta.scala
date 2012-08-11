package cook.config

import java.io.PrintWriter
import scala.io.Source
import scala.tools.nsc.io.Directory


trait ConfigMeta { self: ConfigRef =>

  def getMetaFilePath(metaDir: Directory) = metaDir / (configClassFullName + ".meta")
  def meta(metaDir: Directory) = {
    val p = getMetaFilePath(metaDir)
    val List(cookFileMeta, scalaConfigFileMeta) = Source.fromFile(p.jfile).getLines().take(2).toList
    ConfigMeta.Meta(cookFileMeta, scalaConfigFileMeta)
  }

  def saveMeta(metaDir: Directory, meta: ConfigMeta.Meta) {
    val p = getMetaFilePath(metaDir)
    p.parent.createDirectory()
    p.createFile(false)
    val w = new PrintWriter(p.jfile)
    w.println(meta.cookFileMeta)
    w.println(meta.scalaConfigFileMeta)
    w.close
  }

}

object ConfigMeta {

  case class Meta(cookFileMeta: String, scalaConfigFileMeta: String)
}
