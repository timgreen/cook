package cook.config

import cook.util.ClassPathBuilder

import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.io.Path
import scala.tools.nsc.reporters.ConsoleReporter


object ConfigCompiler {

  lazy val cpBuilder = new ClassPathBuilder

  def compile(configRef: ConfigRef) {
    val outDir = configRef.configByteCodeDir
    prepareOutDir(outDir)
    prevCompileCpUpdate

    val c = new ConfigCompiler(outDir, cpBuilder.classPath)
    c.compile(configRef.configScalaSourceFile)

    postCompileCpUpdate(outDir)
  }

  private def prevCompileCpUpdate {

  }

  private def postCompileCpUpdate(outDir: Path) {
    cpBuilder.add(outDir.path)
  }

  private def prepareOutDir(dir: Path) {
    if (dir.canRead) {
      dir.deleteRecursively
    }
    dir.createDirectory()
  }

  def initDefaultCp = {
    cpBuilder
      .addJavaPath
      .addPathFor(classOf[cook.config.Config])
      .addPathFor(classOf[cook.config.dsl.Dsl])
  }
}

class ConfigCompiler(outDir: Path, cp: String) {

  val settings = generateSettings
  val compiler = new Global(settings, null)

  def compile(file: Path): Unit = {
    synchronized {
      //var messages = List[CompilerError]()
      val reporter = new ConsoleReporter(settings) {

        // override def printMessage(posIn: Position, msg: String) {
        //   val pos = if (posIn eq null) NoPosition
        //             else if (posIn.isDefined) posIn.inUltimateSource(posIn.source)
        //             else posIn
        //   //pos match {
        //   //  case FakePos(fmsg) =>
        //   //    super.printMessage(posIn, msg);
        //   //  case NoPosition =>
        //   //    super.printMessage(posIn, msg);
        //   //  case _ =>
        //   //    messages = CompilerError(posIn.source.file.file.getPath, msg, OffsetPosition(posIn.source.content, posIn.point)) :: messages
        //   //    super.printMessage(posIn, msg);
        //   //}

        // }
      }
      compiler.reporter = reporter

      // Attempt compilation
      (new compiler.Run).compile(List(file.path))

      // Bail out if compilation failed
      if (reporter.hasErrors) {
        reporter.printSummary
        //throw new CompilerException("Compilation failed:\n", messages)
      }
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
}
