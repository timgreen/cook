package cook.config

import cook.path.PathUtil

import java.io.PrintWriter
import scala.io.Source
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


/**
 * Translate Cook config file to scala source.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
class ConfigScalaSourceGenerator(sourceOutputDir: Directory) {

  def generate(configRef: ConfigRef): Path = {
    val source = sourceOutputDir / (configRef.configClassFullName + ".scala")

    withWriter(source) { writer =>
      generateHeader(configRef, writer)
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

  private def generateHeader(configRef: ConfigRef, writer: PrintWriter) {
    writer.println("// GENERATED CODE, DON'T MODIFY")
    writer.println("package %s {  // PACKAGE START" format (configRef.configClassPackageName))
    writer.println("trait %s {  // TRAIT START" format (configRef.configClassTraitName))

    writer.println("// IMPORTS START")
    (ConfigRef.rootConfigRef.imports ++ configRef.imports) foreach { importRef =>
      writer.println("import %s._" format (importRef.configClassFullName))
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
      case ConfigType.CookRootConfig =>
        // nothing to do
      case ConfigType.CookConfig =>
        writer.println("class %s extends %s".format(
          configRef.configClassName, configRef.configClassTraitName))
      case ConfigType.CookiConfig =>
        writer.println("object %s extends %s".format(
          configRef.configClassName, configRef.configClassTraitName))
    }

    writer.println("}  // PACKAGE END")
  }
}

object ConfigScalaSourceGenerator
  extends ConfigScalaSourceGenerator(PathUtil.cookConfigScalaSourceDir)
