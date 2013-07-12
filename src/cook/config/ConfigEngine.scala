package cook.config

import cook.meta.Meta
import cook.meta.MetaHelper
import cook.meta.db.DbProvider.{ db => metaDb }
import cook.util.GlobScanner

import scala.util.Try


/**
 * Generate, compile and load config class.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
private[cook] object ConfigEngine {

  import cook.util.LogSourceProvider._
  import akka.event.Logging

  val log = Logging(cook.app.Global.system, this)

  /**
   * Return config instance for cook, and null for cooki.
   */
  def load(configRef: ConfigRef, rootIncludes: List[ConfigRefInclude],
    depConfigRefs: List[ConfigRef]): Config = {
    val map: Map[String, ConfigRef] = depConfigRefs map { r => r.refName -> r } toMap

    doGenerate(configRef, rootIncludes, map)
    doCompile(configRef, map)
    configRef.configType match {
      case ConfigType.CookConfig =>
        doLoad(configRef, map)
      case ConfigType.CookiConfig =>
        null
    }
  }

  private def doGenerate(configRef: ConfigRef, rootIncludes: List[ConfigRefInclude],
    depConfigRefMap: Map[String, ConfigRef]) {
    if (shouldRegenerateScala(configRef, rootIncludes)) {
      log.debug("config scala source {}: generating", configRef.refName)
      ConfigGenerator.generate(configRef, rootIncludes, depConfigRefMap)
      val meta = buildConfigScalaMeta(configRef, rootIncludes)
      metaDb.put(configRef.configScalaSourceMetaKey, meta)
    } else {
      log.debug("config scala source {}: cached", configRef.refName)
    }
  }

  private def doCompile(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]) {
    if (shouldRecompileScala(configRef, depConfigRefMap)) {
      log.debug("config bytecode {}: building", configRef.refName)
      ConfigCompiler.compile(configRef, depConfigRefMap)
      val meta = buildConfigByteCodeMeta(configRef, depConfigRefMap)
      metaDb.put(configRef.configByteCodeMetaKey, meta)
    } else {
      log.debug("config bytecode {}: cached", configRef.refName)
    }
  }

  private def doLoad(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]): Config = {
    val c = ConfigLoader.load(configRef, depConfigRefMap)
    c
  }

  private def shouldRegenerateScala(
    configRef: ConfigRef, rootIncludes: List[ConfigRefInclude]): Boolean = {
    val meta = buildConfigScalaMeta(configRef, rootIncludes)
    val cachedMeta = metaDb.get(configRef.configScalaSourceMetaKey)
    meta != cachedMeta
  }

  import cook.config.IncludeDefine
  import cook.config.IncludeAsDefine

  private def buildConfigScalaMeta(
    configRef: ConfigRef, rootIncludes: List[ConfigRefInclude]): Meta = {
    val m1 = MetaHelper.buildFileMeta("cookSource", configRef.fileRef.toPath :: Nil)
    val m2 = MetaHelper.buildFileMeta("cookScalaSource", configRef.configScalaSourceFile :: Nil)
    val m3 = new Meta
    rootIncludes foreach { include =>
      include match {
        case IncludeDefine(ref) =>
          m3.add("rootInclude", ref.refName, "")
        case IncludeAsDefine(ref, name) =>
          m3.add("rootIncludeAs", name, ref.refName)
      }
    }
    m1 + m2 + m3
  }

  private def shouldRecompileScala(
    configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]): Boolean = {
    val meta = buildConfigByteCodeMeta(configRef, depConfigRefMap)
    val cachedMeta = metaDb.get(configRef.configByteCodeMetaKey)
    meta != cachedMeta
  }

  private def buildConfigByteCodeMeta(
    configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]): Meta = {
    val m1 = MetaHelper.buildFileMeta("cookScalaSource", configRef.configScalaSourceFile :: Nil)
    val bytecodes = Try {
      GlobScanner(configRef.configByteCodeDir, "**" :: Nil, fileOnly = true)
    } getOrElse Seq()
    val m2 = MetaHelper.buildFileMeta("cookByteCode", bytecodes)
    val m3 = new Meta
    depConfigRefMap foreach { case (refName, ref) =>
      val depMeta = metaDb.get(ref.configByteCodeMetaKey)
      m3.add("depRef", refName, depMeta.hash)
    }
    m1 + m2 + m3
  }
}
