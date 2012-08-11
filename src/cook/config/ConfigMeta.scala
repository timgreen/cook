package cook.config

import cook.path.PathUtil
import cook.util.HashManager

import java.io.PrintWriter
import scala.io.Source
import scala.tools.nsc.io.Directory
import scala.util.control.Exception._


trait ConfigMeta { self: ConfigRef =>

  lazy val metaFilePath = PathUtil().cookConfigMetaDir / (configClassFullName + ".meta")
  lazy val meta = {
    val p = metaFilePath
    allCatch.opt {
      val Seq(cookFileMeta, scalaConfigFileMeta) = Source.fromFile(p.jfile).getLines().take(2).toSeq
      ConfigMeta.Meta(cookFileMeta, scalaConfigFileMeta)
    }
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
    case None => false
    case Some(ConfigMeta.Meta(fh, sh)) =>
      (fh != hash) || (sh != HashManager.hash(configScalaSourceFile))
  }

}

object ConfigMeta {

  case class Meta(cookFileMeta: String, scalaConfigFileMeta: String)
}
