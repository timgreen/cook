package cook.config

import cook.error.ErrorTracking._
import cook.util.ClassPathBuilder

import java.io.PrintWriter
import java.io.StringWriter
import scala.collection.mutable
import scala.io.Source
import scala.reflect.io.AbstractFile
import scala.reflect.io.{ Path => SPath }
import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.util._

object ConfigCompiler {

  def compile(configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]) {
    val outDir = configRef.configByteCodeDir
    prepareOutDir(outDir)

    val cpBuilder = defaultCp.copy()
    addDepCp(cpBuilder, configRef, depConfigRefMap)

    val c = new ConfigCompiler(outDir, cpBuilder.classPath)
    c.compile(configRef.configScalaSourceFile, configRef)
  }

  private def addDepCp(cpBuilder: ClassPathBuilder,
    configRef: ConfigRef, depConfigRefMap: Map[String, ConfigRef]) {
    for (depRef <- depConfigRefMap.values) {
      cpBuilder add depRef.configByteCodeDir.toString
    }
  }

  private def prepareOutDir(dir: SPath) {
    if (dir.canRead) {
      dir.deleteRecursively
    }
    dir.createDirectory()
  }

  def initDefaultCp = {
    (new ClassPathBuilder)
      .addJavaPath
      .addPathFor(classOf[cook.config.Config])
      //.addPathFor(classOf[cook.config.dsl.Dsl])
  }
  private val defaultCp = initDefaultCp
}

class ConfigCompiler(outDir: SPath, cp: String) {
  val compiler = new Global(settings, null)

  def compile(file: SPath, configRef: ConfigRef) {
    val messageCollector = new StringWriter
    val messageCollectorWrapper = new PrintWriter(messageCollector)
    val reporter = new ConsoleReporter(settings, Console.in, messageCollectorWrapper) {

      var _startOffset: Option[Int] = None
      def startOffset(source: SourceFile): Int = _startOffset getOrElse {
        _startOffset = Some(calcStartOffSet(source))
        _startOffset.get
      }
      private def calcStartOffSet(source: SourceFile): Int = {
        val marker = "// BODY START"
        for (i <- 0 until source.length) {
          if (marker == source.lineToString(i)) {
            return source.lineToOffset(i + 1)
          }
        }
        // TODO(timgreen): error
        return 0
      }

      override def printMessage(posIn: Position, msg: String) {
        val pos = if (posIn eq null) NoPosition
                  else if (posIn.isDefined) posIn.inUltimateSource(posIn.source)
                  else posIn
        pos match {
          case FakePos(fmsg) =>
            super.printMessage(posIn, msg);
          case NoPosition =>
            super.printMessage(posIn, msg);
          case _ =>
            val cookSource = new BatchSourceFile(AbstractFile.getFile(
              configRef.fileRef.toPath.path))
            val offset = startOffset(posIn.source)
            val newPos = posIn.withSource(cookSource, -offset)
            if (newPos.point < 0) {
              super.printMessage(new OffsetPosition(cookSource, 0), msg)
            } else {
              super.printMessage(newPos, msg)
            }
        }
      }
    }
    compiler.reporter = reporter

    // Attempt compilation
    (new compiler.Run).compile(List(file.path))

    // Bail out if compilation failed
    if (reporter.hasErrors) {
      reportError("Config Compilation Error: %s\n\n%s", configRef.refName, messageCollector)
    }
  }

  def shutdown = compiler.askShutdown()

  private def errorHandler(message: String): Unit = {
    //throw new TemplateException("Compilation failed:\n" + message)
  }

  private def generateSettings: Settings = {
    val settings = new Settings(errorHandler)
    settings.classpath.value = cp
    settings.outdir.value = outDir.path
    settings.deprecation.value = true
    //settings.unchecked.value = true

    settings.debuginfo.value = "vars"
    settings.dependenciesFile.value = "none"
    settings.debug.value = false

    settings
  }
  val settings = generateSettings
}
