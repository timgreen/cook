package cook.config.dsl.buildin

import cook.ref.{ Ref, TargetRef }
import cook.target.{ Target, TargetResult }

import java.io.File
import scala.io.Source
import scala.reflect.io.{ Path => SPath }
import scala.sys.process.ProcessLogger
import scala.util.{ Try, Success, Failure }

trait Utils {

  def collectTargets(targets: List[Target[TargetResult]], targetRefs: List[Ref]): List[Target[TargetResult]] = {
    val m = targets map { t => t.refName -> t } toMap

    targetRefs map { ref => m(ref.refName) }
  }
  def collectTarget(targets: List[Target[TargetResult]], targetRef: Ref): Target[TargetResult] =
    collectTargets(targets, targetRef :: Nil).head

  def handleBuildCmd(target: Target[TargetResult])(runWithLoggerOp: ProcessLogger => Unit) {
    import scala.sys.process._

    target.ref.logParentDir.createDirectory()
    target.ref.buildLogFile.createFile()
    val f = target.ref.buildLogFile.jfile
    val logger = ProcessLogger(f)
    val r = Try { runWithLoggerOp(logger) }
    logger.flush
    logger.close
    if (r.isFailure) {
      import cook.error._
      import cook.console.ops._
      reportError {
        Source.fromFile(f).mkString
      }
    }
  }
  def runBuildCmdInTargetDir(target: Target[TargetResult])(cmds: Seq[String]*) {
    import scala.sys.process._

    target.ref.logParentDir.createDirectory()
    target.ref.buildLogFile.createFile()
    val f = target.ref.buildLogFile.jfile
    val r = Try {
      cmds foreach { cmd =>
        Process(cmd, Some(target.buildDir.jfile)) #>> f !!
      }
    }
    if (r.isFailure) {
      import cook.error._
      import cook.console.ops._
      reportError {
        Source.fromFile(f).mkString
      }
    }
  }

  def runRunCmdInTargetDir(target: Target[TargetResult])(cmds: Seq[String]*) {
    import scala.sys.process._
    cmds foreach { cmd =>
      val exit = Process(cmd, Some(target.runDir.jfile)) !
      import cook.error._
      import cook.console.ops._
      reportError {
        "Exit with code: " :: strong(exit.toString)
      }
    }
  }
}
