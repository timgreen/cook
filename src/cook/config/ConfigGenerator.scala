package cook.config

import cook.error.ErrorTracking

import java.io.PrintWriter
import scala.io.Source
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


/**
 * Translate Cook config file to scala source.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
object ConfigGenerator extends ErrorTracking {

  def generate(configRef: ConfigRef): Path = {
    val source = configRef.configScalaSourceFile

    withWriter(source) { writer =>
      generateHeader(configRef, writer)
      if (configRef.configType != ConfigType.CookRootConfig) {
        generateImports(configRef, writer)
      }
      generateBody(configRef, writer)
      generateFooter(configRef, writer)
    }

    source
  }

  private def withWriter(source: Path)(op: PrintWriter => Unit) {
    source.parent.createDirectory()
    source.createFile(true)
    val writer = new PrintWriter(source.jfile)
    op(writer)
    writer.close
  }

  val configClassName = "cook.config.Config"
  val baseClassName = "cook.config.dsl.ConfigBase"
  val configContextClassName = "cook.config.dsl.ConfigContext"

  private def generateHeader(configRef: ConfigRef, writer: PrintWriter) {
    writer.println("// GENERATED CODE, DON'T MODIFY")
    writer.println("package %s {  // PACKAGE START" format (configRef.configClassPackageName))

    configRef.configType match {
      case ConfigType.CookConfig =>
        writer.println("trait %s extends %s with %s {  // TRAIT START".format(
          configRef.configClassTraitName,
          configClassName,
          baseClassName
        ))
        val currentConfigRef = "cook.config.ConfigRef(List(%s))" format {
          configRef.segments map {
            // TODO(timgreen): use apache StringUtil?
            _.replace("\\","\\\\")
              .replace("\n","\\n")
              .replace("\b","\\b")
              .replace("\r","\\r")
              .replace("\t","\\t")
              .replace("\'","\\'")
              .replace("\f","\\f")
              .replace("\"","\\\"")
          } mkString ("\"", "\" ,\"", "\"")
        }
        writer.println("override implicit def provideContext: %s = new %s(%s)".format(
          configContextClassName,
          configContextClassName,
          currentConfigRef
        ))
      case ConfigType.CookiConfig =>
        writer.println("trait %s {  // TRAIT START" format (configRef.configClassTraitName))
      case ConfigType.CookRootConfig =>
        writer.println("// TRAIT START")
        writer.println("trait %s".format(configRef.configClassTraitName))
        configRef.mixins map { _.configClassTraitFullName } toList match {
          case h :: t =>
            writer.println("  extends %s" format h)
            t foreach { c =>
              writer.println("  with %s" format c)
            }
          case _ =>
        }
        writer.println("{")
    }

  }

  private def generateImports(configRef: ConfigRef, writer: PrintWriter) {
    writer.println("// IMPORTS START")
    if (configRef.configType == ConfigType.CookConfig) {
      writer.println("val root = %s".format(ConfigRef.rootConfigRef.configClassFullName))
      writer.println("import root._")
    }

    for (importDefine <- configRef.imports) {
      val isPartOfRootMixin = ConfigRef.rootConfigRef.mixins.contains(importDefine.ref)
      val importObject =
        (if (isPartOfRootMixin && (configRef.configType == ConfigType.CookConfig)) {
          ConfigRef.rootConfigRef
        } else {
          importDefine.ref
        }).configClassFullName

      writer.println("val %s: %s = %s".format(
        importDefine.name,
        importDefine.ref.configClassTraitFullName,
        importObject
      ))
      if (importDefine.importMembers) {
        if (isPartOfRootMixin) {
          reportError("Already mixin %s in COOK_ROOT, remove @import or use @val instead",
            importDefine.ref.p.path)
        }
        writer.println("import %s._" format (importDefine.name))
      }
    }
    writer.println("// IMPORTS END")
  }

  private def generateBody(configRef: ConfigRef, writer: PrintWriter) {
    writer.println("// BODY START")
    Source.fromFile(configRef.p.path) getLines() foreach writer.println
    writer.println("// BODY END")
  }

  private def generateFooter(configRef: ConfigRef, writer: PrintWriter) {
    writer.println("}  // TRAIT END")

    configRef.configType match {
      case ConfigType.CookConfig =>
        writer.println("class %s extends %s".format(
          configRef.configClassName, configRef.configClassTraitName))
      case _ =>
        writer.println("object %s extends %s".format(
          configRef.configClassName, configRef.configClassTraitName))
    }

    writer.println("}  // PACKAGE END")
  }
}
