package cook.config

import cook.error.ErrorTracking._
import cook.path.Path
import cook.ref.FileRef
import cook.ref.RefManager

import scala.collection.JavaConversions._
import scala.io.Source
import scala.reflect.io.{ Path => SPath }


object ConfigType extends Enumeration {
  type ConfigType = Value
  val CookConfig, CookiConfig = Value
}

sealed trait ConfigRefInclude {
  val ref: FileRef
}
case class IncludeDefine(ref: FileRef) extends ConfigRefInclude
case class IncludeAsDefine(ref: FileRef, name: String) extends ConfigRefInclude

private[cook] class ConfigRef(val fileRef: FileRef) {

  val configType = fileRef.filename match {
    case "COOK" => ConfigType.CookConfig
    case f if f.endsWith(".cooki") => ConfigType.CookiConfig
    case _ =>
      reportError("This should never happen, unknown config: %s", fileRef.toPath.path)
  }

  val configClassPackageName = (ConfigRef.packagePrefix :: fileRef.dir.segments) mkString "."
  val configClassName = configType match {
    case ConfigType.CookiConfig =>
      fileRef.filename.replace(".", "_")
    case _ =>
      fileRef.filename
  }
  val configClassFullName = configClassPackageName + "." + configClassName
  val configClassTraitName = configClassName + "Trait"
  val configClassTraitFullName = configClassPackageName + "." + configClassTraitName

  val configScalaSourceFile =
    Path().configScalaSourceDir / (configClassFullName + ".scala")
  val configByteCodeDir = Path().configByteCodeDir / configClassFullName

  if (!fileRef.toPath.canRead) {
    reportError("Can not read config: %s", fileRef.toPath.path)
  }
  val includes: Set[ConfigRefInclude] = {
    Source.fromFile(fileRef.toPath.path) getLines() collect {
      case ConfigRef.IncludeP(ref) =>
        IncludeDefine(relativeConfigRef(ref + ".cooki"))
      case ConfigRef.IncludeAsP(name, ref) =>
        IncludeAsDefine(relativeConfigRef(ref + ".cooki"), name)
    } toSet
  }

  def refName = fileRef.refName

  private def relativeConfigRef(ref: String): FileRef = {
    RefManager(fileRef.dir.segments, ref) match {
      case relativeFileRef: FileRef =>
        relativeFileRef
      case _ =>
        reportError("bad import %s", ref)
    }
  }

  private def verify {
    configType match {
      case ConfigType.CookiConfig =>
        if (includes.exists(_.isInstanceOf[IncludeDefine])) {
          reportError("Doesn't support @import define in *.cooki")
        }
        // NOTE: cycle import check will be done in config ref manager actor
      case ConfigType.CookConfig =>  // pass
    }
  }
  verify
}

object ConfigRef {

  val IncludeP   = """^\s*//\s*@import\s+"(.+)"\s*$""".r
  val IncludeAsP = """^\s*//\s*@val\s+(\w+)\s*=\s*"(.+)"\s*$""".r

  val packagePrefix = "COOK_CONFIG_PACKAGE"
}
