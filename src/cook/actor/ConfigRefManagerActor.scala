package cook.actor

import cook.config.ConfigRef
import cook.config.ConfigType
import cook.error.ErrorTracking._

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable

class ConfigRefManagerActor extends Actor {

  private val cache = mutable.Map[String, ConfigRef]()
  private val waitingMap = mutable.Map[String, mutable.ListBuffer[ActorRef]]()
  private val passVerifySet = mutable.Set[String]()

  def receive = {
    case GetConfigRef(cookFileRef) =>
      val refName = cookFileRef.refName
      cache.get(refName) match {
        case Some(configRef) =>
          sender ! configRef
        case None =>
          val list = waitingMap.getOrElseUpdate(refName, mutable.ListBuffer[ActorRef]())
          list += sender
          if (list.size == 1) {
            self ! LoadConfigRef(refName, cookFileRef)
          }
      }
    case ConfigRefLoaded(refName, configRef) =>
      waitingMap.remove(refName) match {
        case None =>
        case Some(list) =>
          for (s <- list) {
            s ! FindConfigRef(configRef)
          }
      }
    case LoadConfigRef(refName, cookFileRef) =>
      val ref = new ConfigRef(cookFileRef)
      cache(refName) = ref
      checkCycleImport(ref)
      sender ! ConfigRefLoaded(refName, ref)
  }

  private def checkCycleImport(ref: ConfigRef) {
    ref.configType match {
      case ConfigType.CookiConfig =>
        val trace = mutable.Set[String]()
        doCycleCheck(trace, ref)
        passVerifySet += ref.fileRef.refName
      case _ => // pass
    }
  }

  private def doCycleCheck(trace: mutable.Set[String], ref: ConfigRef) {
    if (passVerifySet.contains(ref.fileRef.toPath.path)) return

    if (trace.contains(ref.fileRef.toPath.path)) {
      reportError("Found cycle imports in %s", trace.mkString(", "))
    }

    trace += ref.fileRef.toPath.path
    for (d <- ref.imports) {
      val r = cache.getOrElseUpdate(d.ref.refName, new ConfigRef(d.ref))
      doCycleCheck(trace, r)
    }
    trace -= ref.fileRef.refName
  }
}
