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

  def load(configRef: ConfigRef, rootIncludes: List[ConfigRefInclude],
    depConfigRefs: List[ConfigRef]): Config = {
    val map: Map[String, ConfigRef] = depConfigRefs map { r => r.refName -> r } toMap

    doGenerate(configRef, rootIncludes, map)
    doCompile(configRef, map)
    doLoad(configRef, map)
  }

  private def doGenerate(configRef: ConfigRef, rootIncludes: List[ConfigRefInclude],
    depConfigRefMap: Map[String, ConfigRef]) {
    if (shouldRegenerateScala(configRef, rootIncludes)) {
      ConfigGenerator.generate(configRef, rootIncludes, depConfigRefMap)
      val meta = buildConfigScalaMeta(configRef, rootIncludes)
      metaDb.put(configRef.configScalaSourceMetaKey, meta)
    }
  }

  private def doCompile(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]) {
    if (shouldRecompileScala(configRef)) {
      ConfigCompiler.compile(configRef, depConfigRefMap)
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

  private def shouldRecompileScala(configRef: ConfigRef): Boolean = {
    val meta = buildConfigByteCodeMeta(configRef)
    val cachedMeta = metaDb.get(configRef.configByteCodeMetaKey)
    meta != cachedMeta
  }

  private def buildConfigByteCodeMeta(configRef: ConfigRef): Meta = {
    // TODO(timgreen): add include & rootInclude meta info
    val m1 = MetaHelper.buildFileMeta("cookScalaSource", configRef.configScalaSourceFile :: Nil)
    val bytecodes = Try {
      GlobScanner(configRef.configByteCodeDir, "**/*" :: Nil, fileOnly = true)
    } getOrElse Seq()
    val m2 = MetaHelper.buildFileMeta("cookByteCode", bytecodes)
    m1 + m2
  }
}
