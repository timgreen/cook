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
  val CookRootConfig, CookConfig, CookiConfig = Value
}

sealed trait ConfigRefImport {
  val ref: FileRef
}
case class ImportDefine(ref: FileRef) extends ConfigRefImport
case class ValDefine(ref: FileRef, name: String) extends ConfigRefImport

private[cook] class ConfigRef(val fileRef: FileRef) {

  val configType = fileRef.filename match {
    case "COOK_ROOT" => ConfigType.CookRootConfig
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
  val (imports, mixins): (Set[ConfigRefImport], Set[FileRef]) = {
    val list = Source.fromFile(fileRef.toPath.path) getLines() collect {
      case ConfigRef.ImportP(ref) =>
        ImportDefine(relativeConfigRef(ref + ".cooki"))
      case ConfigRef.ValP(name, ref) =>
        ValDefine(relativeConfigRef(ref + ".cooki"), name)
      case ConfigRef.MixinP(ref) =>
        relativeConfigRef(ref + ".cooki")
    } toList

    val importsList = list collect {
      case x: ConfigRefImport => x
    }
    val mixinsList = list collect {
      case x: FileRef => x
    }

    (importsList.toSet, mixinsList.toSet)
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
      case ConfigType.CookRootConfig =>
        Source.fromFile(fileRef.toPath.path) getLines() forall { line =>
          (line matches ConfigRef.MixinP.toString) ||
            (line matches """^\s*//.*$""") ||
            (line matches """^\s*$""")
        } match {
          case true =>
          case false =>
            // NOTE(timgreen): only always mixins in cook root config for now.
            reportError("Only always mixins in COOK_ROOT config for now.")
        }
      case ConfigType.CookiConfig =>
        if (imports.exists(_.isInstanceOf[ImportDefine])) {
          reportError("Doesn't support @import define in *.cooki")
        }
        // NOTE: cycle import check will be done in config ref manager actor
      case ConfigType.CookConfig =>  // pass
    }
  }
  verify
}

object ConfigRef {

  val rootConfigFileRef = RefManager(Nil, "/COOK_ROOT").as[FileRef]

  val ImportP = """^\s*//\s*@import\s+"(.+)"\s*$""".r
  val ValP    = """^\s*//\s*@val\s+(\w+)\s*=\s*"(.+)"\s*$""".r
  val MixinP  = """^\s*//\s*@mixin\s+"(.+)"\s*$""".r

  val packagePrefix = "COOK_CONFIG_PACKAGE"
}
