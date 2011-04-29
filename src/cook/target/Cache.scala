package cook.target

import java.io.File
import java.io.PrintStream

import scala.collection.Iterator
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.io.Source
import scala.util.control._

abstract class Line
case class Section(name: String) extends Line
case class SetValue(item: String) extends Line
case class MapValue(item: String, timestamp: Long) extends Line
case class EndOfFile() extends Line

class Cache(metaFile: File) {

  def read() {
    val lineIt = Source.fromFile(metaFile).getLines

    if (readline(lineIt) != Section("deps")) {
      throw new TargetException("Cache meta file format error, except section header deps")
    }

    val mybreaks = new Breaks
    import mybreaks.{break, breakable}


    breakable {
      while (true) {
        readline(lineIt) match {
          case SetValue(i) => deps += i
          case Section("inputs") => break
          case _ => invalid
        }
      }
    }

    breakable {
      while (true) {
        readline(lineIt) match {
          case MapValue(i, t) => inputs.put(i, t)
          case Section("tools") => break
          case _ => invalid
        }
      }
    }

    breakable {
      while (true) {
        readline(lineIt) match {
          case MapValue(i, t) => tools.put(i, t)
          case EndOfFile() => break
          case _ => invalid
        }
      }
    }
  }

  def write() {
    val p = new PrintStream(metaFile)
    p.println("deps:")
    for (i <- deps) { p.println("  %s".format(i)) }
    p.println
    p.println("inputs:")
    for ((i, t) <- inputs) { p.println("  %s: %d".format(i, t)) }
    p.println
    p.println("tools:")
    for ((i, t) <- tools) { p.println("  %s: %d".format(i, t)) }
    p.close
  }

  val deps = new HashSet[String]
  val inputs = new HashMap[String, Long]
  val tools = new HashMap[String, Long]

  private
  val SectionPattern = "(.+):".r
  val SetValuePattern = "  (.+)".r
  val MapValuePattern = "  (.+): (.+)".r

  def readline(lineIt: Iterator[String]): Line = {
    while (lineIt.hasNext) {
      lineIt.next match {
        case SectionPattern(name) => return Section(name)
        case MapValuePattern(item, timestamp) => return MapValue(item, timestamp.toLong)
        case SetValuePattern(item) => return SetValue(item)
        case "" => None
        case _ => invalid
      }
    }

    EndOfFile()
  }

  def invalid = {
    throw new TargetException("Invalid cache meta file \"%s\"", metaFile.getPath)
  }
}
