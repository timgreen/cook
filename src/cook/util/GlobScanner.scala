/**
 * @fileoverview
 *
 * According to http://code.google.com/p/wildcard/source/browse/trunk/src/com/esotericsoftware/wildcard/GlobScanner.java
 */

package cook.util

import scala.annotation.tailrec
import scala.collection.mutable
import scala.reflect.io.{ Path => SPath, Directory }


case class Pattern(val values: Array[String], index: Int = 0) {

  val value = if (index < values.length) values(index) else null

  def this(pattern: String) {
    this(pattern.replace('\\', '/')
      .replaceAll("\\*{3,}", "**")
      .replaceAll("\\*\\*/", "**/*/")
      .replaceAll("\\*\\*([^/])", "**/*$1")
      .replaceAll("/\\*\\*", "/*/**")
      .replaceAll("([^/])\\*\\*", "$1*/**")
      .split("/"))
  }

  def matches(filename: String): Boolean = {
    if (value == "**") return true
    if (value == null) return false

    // Shortcut if no wildcards.
    if ((value.indexOf('*') == -1) && (value.indexOf('?') == -1)) {
      return filename == value
    }

    @tailrec
    def doMatches(indexes: mutable.Set[Int], it: Iterator[Char]): Boolean = {
      if (!it.hasNext) {
        indexes.contains(value.length)
      } else {
        val c = it.next
        val nextIndexes = mutable.Set[Int]()
        for (i <- indexes if i < value.length) {
          if (value(i) == '*') {
            nextIndexes += i
            nextIndexes += i + 1
            if (i + 1 < value.length && (value(i + 1) == '?' || value(i + 1) == c)) {
              nextIndexes += i + 2
            }
          } else if (value(i) == '?' || value(i) == c) {
            nextIndexes += i + 1
          }
        }
        doMatches(nextIndexes, it)
      }
    }
    doMatches(mutable.Set(0), filename.iterator)
  }

  def next(name: String): Seq[Pattern] = {
    if (isEnd) {
      Seq()
    } else {
      val nextPattern = buildNext
      if (value == "**") {
        if (nextPattern.matches(name)) {
          Seq(this, nextPattern, nextPattern.buildNext)
        } else {
          Seq(this, nextPattern)
        }
      } else {
        Seq(nextPattern)
      }
    }
  }

  def isSimplyPattern = (value != null) && (value.indexOf('*') == -1) && (value.indexOf('?') == -1)
  def isMatched: Boolean = {
    (values.length == index + 1) || ((values.length == index + 2) && (values.last == "**"))
  }
  def isLast = (values.length == index + 1)
  def isEnd: Boolean = values.length <= index

  private def buildNext: Pattern = Pattern(values, index + 1)
}

class GlobScanner(rootDir: Directory, includes: Seq[String], excludes: Seq[String]) {

  initCheck

  private def initCheck {
    if (rootDir == null) throw new IllegalArgumentException("rootDir cannot be null.")
    if (!rootDir.exists) throw new IllegalArgumentException("Directory does not exist: " + rootDir)

    if (includes == null) throw new IllegalArgumentException("includes cannot be null.");
    if (excludes == null) throw new IllegalArgumentException("excludes cannot be null.");
  }

  private def scanDir(dir: Directory, includes: Seq[Pattern], excludes: Seq[Pattern],
    matches: mutable.ListBuffer[SPath]) {
    if (!dir.canRead) return

    // See has excludes all
    if (excludes exists { e => e.isLast && e.value == "**" }) return

    // See if patterns are specific enough to avoid scanning every file in the directory.
    val scanAll = includes exists { !_.isSimplyPattern }

    if (!scanAll) {
      // If not scanning all the files, we know exactly which ones to follow.
      includes.groupBy(_.value).foreach { kv =>
        val name = kv._1
        process(dir / name, kv._2, excludes.filter(_.matches(name)), matches);
      }
    } else {
      // Scan every file.
      for (path <- dir.list) {
        // Get all include patterns that match.
        val name = path.name
        val matchingIncludes = includes.filter(_.matches(name))
        if (matchingIncludes.nonEmpty) {
          process(path, matchingIncludes, excludes.filter(_.matches(name)), matches)
        }
      }
    }
  }

  private def process(path: SPath, matchingIncludes: Seq[Pattern],
    matchingExcludes: Seq[Pattern], matches: mutable.ListBuffer[SPath]) {
    if (!path.exists) return

    if (matchingIncludes.exists(_.isMatched) && !matchingExcludes.exists(_.isMatched)) {
      // match found!
      matches += path
    }

    if (path.isDirectory) {
      val nextIncludes = mutable.Set[Pattern]()
      val nextExcludes = mutable.Set[Pattern]()
      matchingIncludes.foreach { nextIncludes ++= _.next(path.name) }
      matchingExcludes.foreach { nextExcludes ++= _.next(path.name) }
      scanDir(path.toDirectory, nextIncludes.toSeq, nextExcludes.toSeq, matches)
    }
  }

  def scan(fileOnly: Boolean): Seq[SPath] = {
    val includePatterns = (includes match {
      case List() => List("**")
      case _ => includes
    }).map { new Pattern(_) }
    val excludePatterns = excludes.map(new Pattern(_))

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
}
