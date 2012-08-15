package cook.util

import java.io.{ File => JFile }
import java.net.{URI, URLClassLoader}
import scala.collection.mutable


/**
 * Copy from https://github.com/scalate/scalate/blob/master/scalate-util/src/main/scala/org/fusesource/scalate/util/ClassPathBuilder.scala
 */
class ClassPathBuilder {
  import ClassPathBuilder._

  def classPath = cp mkString JFile.pathSeparator

  private def addEntry(path: String) = {
    if (!cpSet.contains(path)) {
      cp += path
      cpSet += path
    }
    this
  }

  def add(paths: String*): ClassPathBuilder = {
    paths foreach addEntry
    this
  }

  def addPathFor(clazz: Class[_]): ClassPathBuilder = {
    if (clazz != null) {
      addPathFrom(clazz.getClassLoader)
    }
    this
  }

  def addPathFrom(loader: ClassLoader): ClassPathBuilder = {
    add(getClassPathFrom(loader): _*)
    this
  }

  def addJavaPath = add(javaClassPath: _*)
  def javaClassPath: Seq[String] = {
    sys.props.get("java.class.path") filter {
      _.nonEmpty
    } match {
      case Some(jcp) => jcp.split(JFile.pathSeparator)
      case None => Nil
    }
  }


  private [util] val cp = mutable.ArrayBuffer[String]()
  private [util] val cpSet = mutable.Set[String]()

}

object ClassPathBuilder {

  def getClassPathFrom(classLoader: ClassLoader): Seq[String] = classLoader match {
    case null => Nil
    case cl: URLClassLoader =>
      for (
        url <- cl.getURLs.toList;
        uri = new URI(url.toString);
        path = uri.getPath
        if (path != null)
      ) yield {

        // on windows the path can include %20
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4466485
        // so lets use URI as a workaround
        new JFile(path).getCanonicalPath
        //val n = new File(uri.getPath).getCanonicalPath
        //if (n.contains(' ')) {"\"" + n + "\""} else {n}
      }

    case _ =>
      //warn("Cannot introspect on class loader: %s of type %s", classLoader, classLoader.getClass.getCanonicalName)
      println("Cannot introspect on class loader: %s of type %s".format(classLoader, classLoader.getClass.getCanonicalName))
      val parent = classLoader.getParent
      if ((parent != null) && (parent != classLoader)) {
        getClassPathFrom(parent)
      } else {
        Nil
      }
  }
}
