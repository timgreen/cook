package cook.config

import cook.path.PathUtil
import cook.util.HashManager

import java.io.PrintWriter
import scala.io.Source
import scala.tools.nsc.io.Directory
import scala.util.control.Exception._


trait ConfigMeta { self: ConfigRef =>

  lazy val metaFilePath = PathUtil().cookConfigMetaDir / (configClassFullName + ".meta")
  def meta = {
    if (meta_ == null) {
      meta_ = loadMeta
    }
    meta_
  }
  private var meta_ : Option[ConfigMeta.Meta] = null
  private def loadMeta = {
    val p = metaFilePath
    allCatch.opt {
      val Seq(cookFileMeta, scalaConfigFileMeta) = Source.fromFile(p.jfile).getLines().take(2).toSeq
      ConfigMeta.Meta(cookFileMeta, scalaConfigFileMeta)
    }
  }

  def saveMeta {
    val m = ConfigMeta.Meta(hash, HashManager.hash(configScalaSourceFile, force = true))
    meta_ = Some(m)
    doSaveMeta(m)
  }

  private def doSaveMeta(meta: ConfigMeta.Meta) {
    metaFilePath.parent.createDirectory()
    metaFilePath.createFile(false)
    val w = new PrintWriter(metaFilePath.jfile)
    w.println(meta.cookFileMeta)
    w.println(meta.scalaConfigFileMeta)
    w.close
  }

  def shouldGenerateScala = meta match {
    case None => true
    case Some(ConfigMeta.Meta(fh, sh)) =>
      (fh != hash) || (sh != HashManager.hash(configScalaSourceFile))
  }

}

object ConfigMeta {

  case class Meta(cookFileMeta: String, scalaConfigFileMeta: String)
}
