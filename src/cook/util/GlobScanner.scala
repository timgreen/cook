/** @fileoverview
  *
  * According to http://code.google.com/p/wildcard/source/browse/trunk/src/com/esotericsoftware/wildcard/GlobScanner.java
  */

package cook.util

import scala.annotation.tailrec
import scala.collection.mutable
import scala.reflect.io.{ Path => SPath, Directory }

class GlobScanner(rootDir: Directory, includes: Seq[String], excludes: Seq[String]) {

  import GlobScanner._

  require(rootDir != null,  "rootDir cannot be null.")
  require(rootDir.exists,   "Directory does not exist: " + rootDir)
  require(includes != null, "includes cannot be null.")
  require(excludes != null, "excludes cannot be null.")

  private def scanDir(dir: Directory, includes: Seq[Pattern], excludes: Seq[Pattern],
    matches: mutable.ListBuffer[SPath]) {
    if (!dir.canRead) return
    // See has excludes all
    if (excludes exists { _.hasMatchAll }) return

    // Scan every file.
    for (path <- dir.list) {
      // Get all include patterns that match.
      val name = path.name
      val matchingIncludes = includes map { _.matches(name) } filterNot { _.isEmpty }
      if (matchingIncludes.nonEmpty) {
        process(path, matchingIncludes, excludes.map(_.matches(name)).filterNot(_.isEmpty), matches)
      }
    }
  }

  private def process(path: SPath, matchingIncludes: Seq[Pattern],
    matchingExcludes: Seq[Pattern], matches: mutable.ListBuffer[SPath]) {
    if (!path.exists) return

    if (matchingIncludes.exists(_.hasMatched) && !matchingExcludes.exists(_.hasMatched)) {
      // match found!
      matches += path
    }

    if (path.isDirectory) {
      val nextIncludes = matchingIncludes map { _.matches("/") } filterNot { _.isEmpty }
      val nextExcludes = matchingExcludes map { _.matches("/") } filterNot { _.isEmpty }
      scanDir(path.toDirectory, nextIncludes, nextExcludes, matches)
    }
  }

  def scan(fileOnly: Boolean): Seq[SPath] = {
    val includePatterns = (includes match {
      case Nil => List("**")
      case _ => includes
    }).map(Pattern(_))
    val excludePatterns = excludes.map(Pattern(_))

    val matches = mutable.ListBuffer[SPath]()
    scanDir(rootDir, includePatterns, excludePatterns, matches)

    matches filter { path =>
      !fileOnly || path.isFile
    }
  }
}

object GlobScanner {
  def apply(dir: Directory,
    includes: Seq[String] = Seq(),
    excludes: Seq[String] = Seq(),
    fileOnly: Boolean = false) =
    new GlobScanner(dir, includes, excludes).scan(fileOnly)

  sealed trait Item {
    def nextDelta(c: Char): Set[Int]
    def canPass: Boolean
  }
  case class CharItem(c: Char) extends Item {
    override def nextDelta(c: Char): Set[Int] = if (this.c == c) {
      Set(1)
    } else {
      Set()
    }
    override def canPass: Boolean = false
  }
  case object ** extends Item {
    override def nextDelta(c: Char): Set[Int] = Set(0, 1)
    override def canPass: Boolean = true
  }
  case object * extends Item {
    override def nextDelta(c: Char): Set[Int] = if (c != '/') Set(0, 1) else Set()
    override def canPass: Boolean = true
  }
  case object ? extends Item {
    override def nextDelta(c: Char): Set[Int] = if (c != '/') Set(1) else Set()
    override def canPass: Boolean = false
  }

  case class Pattern(items: List[Item], indexes: Set[Int]) {
    def isEmpty = indexes.isEmpty
    def hasMatched: Boolean = indexes.contains(items.length)
    def hasMatchAll: Boolean = (items.lastOption == Some(**)) && indexes.contains(items.size - 1)

    def matches(s: String): Pattern = {
      @tailrec
      def doMatches(indexes: Set[Int], it: Iterator[Char]): Set[Int] = {
        val ei = extendIndexes(indexes)
        if (!it.hasNext) {
          ei
        } else {
          val c = it.next

          val nextIndexes = (ei map { i =>
            if (i < items.size) {
              items(i).nextDelta(c).map(_ + i)
            } else {
              Set[Int]()
            }
          }).flatten.toSet

          doMatches(nextIndexes, it)
        }
      }
      this.copy(indexes = doMatches(indexes, s.iterator))
    }

    def extendedIndexes = extendIndexes(indexes)
    private def extendIndexes(indexes: Set[Int]): Set[Int] = {
      val builder = Set.newBuilder[Int]
      val known = mutable.Set(indexes.toSeq: _*)
      val unprocessed = mutable.Stack(indexes.toSeq: _*)
      while (unprocessed.nonEmpty) {
        val i = unprocessed.pop
        builder += i
        if ((i < items.size) && items(i).canPass && !known.contains(i + 1)) {
          unprocessed.push(i + 1)
          known += (i + 1)
        }
      }
      builder.result
    }
  }

  object Pattern {

    def apply(pattern: String) = {
      val p = pattern.replace('\\', '/').replaceAll("\\*{3,}", "**")
      val (l, last) = p.foldLeft[(List[Item], Item)](Nil -> null) { case ((list, last), c) =>
        if (c == '*' && last == *) {
          list -> **
        } else {
          val item = c match {
            case '*' => *
            case '?' => ?
            case _ => CharItem(c)
          }
          (list ::: last :: Nil) -> item
        }
      }
      new Pattern(l.tail ::: last :: Nil, Set(0))
    }
  }
}
