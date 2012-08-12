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

private[config] class ConfigRef(segments: List[String]) extends PathRef(segments) with ConfigMeta {

  private def verify {
    if (!p.canRead) {
      // TODO(timgreen): report error
      throw new Exception("a")
    }

    ConfigRef.checkCycleImport(this);

    if (configType == ConfigType.CookRootConfig) {
      Source.fromFile(p.path) getLines() forall {
        _ matches ConfigRef.ImportP.toString
      } match {
        case true =>
        case false =>
          // TODO(timgreen): only always imports in cook root config for now.
          throw new Exception("b")
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
  lazy val configScalaSourceFile =
    PathUtil().cookConfigScalaSourceDir / (configClassFullName + ".scala")
  lazy val configClassFilesDir = PathUtil().cookConfigClassDir / configClassFullName

  lazy val imports: Set[ConfigRef] = loadImports
  private def loadImports = {
    Source.fromFile(p.path) getLines() collect {
      case ConfigRef.ImportP(importName) =>
        relativeConfigRef(importName + ".cooki")
    } toSet
  }

  private def relativeConfigRef(importName: String) = {
    ConfigRef(relativePathRefSegments(importName))
  }
}

object ConfigRef {

  def apply(path: Path): ConfigRef = cache.get(path.path) match {
    case Some(c) => c
    case None =>
      val c = createConfigRef(path)
      cache(path.path) = c
      c.verify
      c
  }
  def apply(segments: List[String]): ConfigRef =
    apply(PathUtil().relativeToRoot(segments: _*))

  val ImportP = """\s*//\s*@import\("(.*)"\)\s*$""".r
  def rootConfigRef = apply(List("COOK_ROOT"))

  private def createConfigRef(path: Path) = {
    new ConfigRef(PathUtil().relativeToRoot(path))
  }

  private [config] val cache: mutable.ConcurrentMap[String, ConfigRef] =
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
      throw new Exception("c")
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
