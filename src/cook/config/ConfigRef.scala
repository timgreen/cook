package cook.config

import cook.path.PathRef
import cook.path.PathUtil

import java.util.concurrent.{ ConcurrentHashMap => JConcurrentHashMap }
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.io.Source
import scala.tools.nsc.io.Path


object ConfigType extends Enumeration {
  type ConfigType = Value
  val CookRootConfig, CookConfig, CookiConfig = Value
}

private[config] class ConfigRef(segments: List[String]) extends PathRef(segments) {

  private def verify {
    if (!p.canRead) {
      // TODO(timgreen): report error
    }

    ConfigRef.checkCycleImport(this);

    if (configType == ConfigType.CookRootConfig) {
      Source.fromFile(p.path) getLines() forall {
        _ matches ConfigRef.ImportP.toString
      } match {
        case true =>
        case false =>
          // TODO(timgreen): only always imports in cook root config for now.
      }
    }
  }

  lazy val configType = segments.last match {
    case "COOK_ROOT" => ConfigType.CookRootConfig
    case "COOK" => ConfigType.CookConfig
    case f if f.endsWith(".cooki") => ConfigType.CookiConfig
    case _ =>
      // TODO(timgreen): error
      throw new Exception("error")
  }

  val packagePrefix = "COOK_CONFIG_PACKAGE"
  lazy val configClassPackageName = (packagePrefix :: segments.dropRight(1)) mkString "."
  lazy val configClassFullName = configClassPackageName + "." + configClassName
  lazy val configClassName = configType match {
    case ConfigType.CookiConfig =>
      segments.last.replace(".", "_")
    case _ =>
      segments.last
  }
  lazy val configClassTraitName = configClassName + "Trait"

  lazy val imports = loadImports
  private def loadImports: List[ConfigRef] = {
    Source.fromFile(p.path) getLines() collect {
      case ConfigRef.ImportP(importName) =>
        relativeConfigRef(importName + ".cooki")
    } toList
  }

  private def relativeConfigRef(importName: String)(implicit pathUtil: PathUtil) = {
    ConfigRef(relativePathRefSegments(importName))
  }
}

object ConfigRef {

  def apply(path: Path): ConfigRef = cache getOrElseUpdate (path.path, createConfigRef(path))
  def apply(segments: List[String])(implicit pathUtil: PathUtil): ConfigRef =
    apply(pathUtil.relativeToRoot(segments: _*))

  val ImportP = """\s*//\s*@import\("(.*)"\)\s*$""".r
  def rootConfigRef = apply(List("COOK_ROOT"))

  private def createConfigRef(path: Path)(implicit pathUtil: PathUtil) = {
    val c = new ConfigRef(pathUtil.relativeToRoot(path))
    c.verify
    c
  }

  private val cache: mutable.ConcurrentMap[String, ConfigRef] =
    new JConcurrentHashMap[String, ConfigRef]

  val cycleCheckPassed = mutable.Set[String]()
  private def checkCycleImport(ref: ConfigRef) {
    ref.configType match {
      case ConfigType.CookiConfig =>
        val trace = mutable.Set[String]()
        doCycleCheck(trace, ref)
        cycleCheckPassed += ref.p.path
      case _ => // pass
    }
  }

  private def doCycleCheck(trace: mutable.Set[String], ref: ConfigRef): Unit = {
    if (cycleCheckPassed.contains(ref.p.path)) {
      return
    }

    if (trace.contains(ref.p.path)) {
        // TODO(timgreen): report error
      return
    }

    trace += ref.p.path
    for (d <- ref.imports) {
      doCycleCheck(trace, d)
    }
    trace -= ref.p.path
  }
}
