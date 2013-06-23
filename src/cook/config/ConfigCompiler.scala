package cook.config

import cook.console.ops._
import cook.error._
import cook.util.ClassPathBuilder

import java.io.PrintWriter
import java.io.StringWriter
import scala.collection.mutable
import scala.io.Source
import scala.reflect.io.AbstractFile
import scala.reflect.io.{ Path => SPath }
import scala.reflect.internal.util._
import scala.tools.nsc.Settings
import scala.tools.nsc.Global
import scala.tools.nsc.reporters.ConsoleReporter

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
      .addPathFor(cook.config.dsl.Dsl.getClass)
  }
  private val defaultCp = initDefaultCp
}

class ConfigCompiler(outDir: SPath, cp: String) {

  def compile(file: SPath, configRef: ConfigRef) {
    val messageCollector = new StringWriter
    val reporter = new ConsoleReporter(settings, Console.in, new PrintWriter(messageCollector)) {

      var _startOffset: Option[Int] = None
      def startOffset(source: SourceFile): Int = _startOffset getOrElse {
        _startOffset = Some(calcStartOffSet(source))
        _startOffset.get
      }
      private def calcStartOffSet(source: SourceFile): Int = {
        val marker = "// {{{ BODY START"
        0 until source.length find { i =>
          marker == source.lineToString(i)
        } map { i =>
          source.lineToOffset(i + 1)
        } getOrError {
          red("Our base is under attack!!!") :: newLine ::
          indent :: "generated config scala source has been modified: " :: strong(file.path)
        }
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
    val compiler = new Global(settings, reporter)

    // Attempt compilation
    (new compiler.Run).compile(List(file.path))

    // Bail out if compilation failed
    reportErrorIf(reporter.hasErrors) {
      "Config Compilation Error: " :: strong(configRef.refName) ::
      newLine :: newLine ::
      messageCollector.toString
    }
  }

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
