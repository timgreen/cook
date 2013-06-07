package cook.config

import cook.error.ErrorTracking._

import java.io.PrintWriter
import scala.io.Source
import scala.reflect.io.{ Path => SPath, Directory }


/**
 * Translate Cook config file to scala source.
 *
 * @author iamtimgreen@gmail.com (Tim Green)
 */
object ConfigGenerator {

  def generate(configRef: ConfigRef, rootIncludes: List[ConfigRefInclude],
    depConfigRefsMap: Map[String, ConfigRef]) {
    withWriter(configRef) { writer =>
      generateHeader(configRef, depConfigRefsMap, writer)
      generateIncludes(configRef, rootIncludes, depConfigRefsMap, writer)
      generateBody(configRef, depConfigRefsMap, writer)
      generateFooter(configRef, depConfigRefsMap, writer)
    }
  }

  private def withWriter(configRef: ConfigRef)(op: PrintWriter => Unit) {
    val source = configRef.configScalaSourceFile
    source.parent.createDirectory()
    source.createFile(true)
    val writer = new PrintWriter(source.jfile)
    op(writer)
    writer.close
  }

  val configClassName = classOf[cook.config.Config].getName
  val configContextClassName = classOf[cook.config.dsl.ConfigContext].getName
  val dslClassName = cook.config.dsl.Dsl.getClass.getName.split("\\$").last

  private def generateHeader(configRef: ConfigRef, depConfigRefsMap: Map[String, ConfigRef],
    writer: PrintWriter) {
    writer.println("// GENERATED CODE, DON'T MODIFY")
    writer.println("package %s {  // PACKAGE START" format (configRef.configClassPackageName))

    configRef.configType match {
      case ConfigType.CookConfig =>
        writer.println("trait %s extends %s {  // TRAIT START".format(
          configRef.configClassTraitName,
          configClassName
        ))
        val cookFileRef = "cook.ref.RefManager(\"%s\").as[cook.ref.FileRef]" format {
          configRef.fileRef.refName
        }
        writer.println("override implicit val context: %s = new %s(%s)".format(
          configContextClassName,
          configContextClassName,
          cookFileRef
        ))
      case ConfigType.CookiConfig =>
        writer.println("trait %s extends %s {  // TRAIT START".format(
          configRef.configClassTraitName,
          dslClassName
        ))
    }

  }

  private def generateIncludes(configRef: ConfigRef, rootIncludes: List[ConfigRefInclude],
    depConfigRefsMap: Map[String, ConfigRef],
    writer: PrintWriter) {

    def writeInclude(include: ConfigRefInclude) {
      include match {
        case IncludeDefine(ref) =>
          writer.println("import %s._".format(depConfigRefsMap(ref.refName).configClassFullName))
        case IncludeAsDefine(ref, name) =>
          writer.println("val %s: %s = %s".format(
            name,
            depConfigRefsMap(ref.refName).configClassTraitFullName,
            depConfigRefsMap(ref.refName).configClassFullName
          ))
      }
    }

    if (configRef.configType == ConfigType.CookConfig) {
      writer.println("// {{{ ROOT INCLUDES START")
      rootIncludes foreach writeInclude
      writer.println("// }}} ROOT INCLUDES END")
    }

    writer.println("// {{{ INCLUDES START")
    configRef.includes foreach writeInclude
    writer.println("// }}} INCLUDES END")
  }

  private def generateBody(configRef: ConfigRef, depConfigRefsMap: Map[String, ConfigRef],
    writer: PrintWriter) {
    writer.println("// {{{ BODY START")
    writer.println("import %s._".format(dslClassName))
    Source.fromFile(configRef.fileRef.toPath.path) getLines() foreach writer.println
    writer.println("// }}} BODY END")
  }

  private def generateFooter(configRef: ConfigRef, depConfigRefsMap: Map[String, ConfigRef],
    writer: PrintWriter) {
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
