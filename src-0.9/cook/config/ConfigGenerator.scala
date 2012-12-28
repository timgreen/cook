package cook.config

import cook.error.ErrorTracking._

import java.io.PrintWriter
import scala.io.Source
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


/**
 * Translate Cook config file to scala source.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
object ConfigGenerator {

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

  val configClassName = classOf[cook.config.Config].getName
  val configContextClassName = classOf[cook.config.dsl.ConfigContext].getName
  val dslClassName = classOf[cook.config.dsl.Dsl].getName

  private def generateHeader(configRef: ConfigRef, writer: PrintWriter) {
    writer.println("// GENERATED CODE, DON'T MODIFY")
    writer.println("package %s {  // PACKAGE START" format (configRef.configClassPackageName))

    configRef.configType match {
      case ConfigType.CookConfig =>
        writer.println("trait %s extends %s with %s {  // TRAIT START".format(
          configRef.configClassTraitName,
          configClassName,
          dslClassName
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
        writer.println("override implicit val context: %s = new %s(%s)".format(
          configContextClassName,
          configContextClassName,
          currentConfigRef
        ))
      case ConfigType.CookiConfig =>
        writer.println("trait %s extends %s {  // TRAIT START".format(
          configRef.configClassTraitName,
          dslClassName
        ))
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
      writer.println("//// ROOT IMPORTS START")
      ConfigRef.rootConfigRef.mixins foreach { ref =>
        writer.println("import %s._".format(ref.configClassFullName))
      }
      writer.println("//// ROOT IMPORTS END")
    }

    for (importDefine <- configRef.imports) {
      importDefine match {
        case ImportDefine(ref) =>
          val isPartOfRootMixin = ConfigRef.rootConfigRef.mixins.contains(importDefine.ref)
          if (!isPartOfRootMixin) {
            writer.println("import %s._".format(ref.configClassFullName))
          }
        case ValDefine(ref, name) =>
          writer.println("val %s: %s = %s".format(
            name,
            ref.configClassTraitFullName,
            ref.configClassFullName
          ))
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
