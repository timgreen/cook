
/**
 * @fileoverview
 *
 * According to http://code.google.com/p/wildcard/source/browse/trunk/src/com/esotericsoftware/wildcard/GlobScanner.java
 */

package cook.util

import scala.collection.mutable
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path


class Pattern(pattern: String) {

  val values = toValues(pattern)
  var value = values.head
  var index = 0

  private def toValues(pattern: String): Array[String] = {
    pattern.replace('\\', '/')
      .replaceAll("\\*\\*[^/]", "**/*")
      .replaceAll("[^/]\\*\\*", "*/**")
      .split("/");
  }

  def matches(filename: String): Boolean = {
    if (value == "**") return true

    // Shortcut if no wildcards.
    if ((value.indexOf('*') == -1) && (value.indexOf('?') == -1)) {
      return filename == value
    }

    var i = 0
    var j = 0
    while (i < filename.length && j < value.length && value(j) != '*') {
      if (value(j) != filename(i) && value(j) != '?') return false
      i += 1
      j += 1
    }

    // If reached end of pattern without finding a * wildcard, the match has to fail if not same length.
    if (j == value.length) return filename.length == value.length

    var cp = 0
    var mp = 0
    while (i < filename.length) {
      if (j < value.length && value(j) == '*') {
        val jj = j
        j += 1

        if (jj >= value.length) return true
        mp = j
        cp = i + 1
      } else if (j < value.length && (value(j) == filename(i) || value(j) == '?')) {
        j += 1
        i += 1
      } else {
        j = mp
        i = cp
        cp += 1
      }
    }

    // Handle trailing asterisks.
    while (j < value.length && value(j) == '*') j += 1

    j >= value.length
  }

  def incr(filename: String): Boolean = {
    if (value == "**") {
      if (index == values.length - 1) return false
      incr

      if (matches(filename)) {
        incr
      } else {
        decr
        return false
      }
    } else {
      incr
    }

    true
  }

  def incr {
    index += 1

    if (index >= values.length) {
      value = null
    } else {
      value = values(index)
    }
  }

  def decr {
    index -= 1

    if (index > 0 && (values(index - 1) == "**")) {
      index -= 1
    } else {
      value = values(index)
    }
  }

  def reset {
    index = 0
    value = values(0)
  }

  def isExhausted: Boolean = index >= values.length
  def isLast: Boolean = index >= values.length - 1
  def wasFinalMatch: Boolean = isExhausted || (isLast && value == "**")
}

class GlobScanner(rootDir: Directory, includes: Seq[String], excludes: Seq[String]) {

  initCheck

  private def initCheck {
    if (rootDir == null) throw new IllegalArgumentException("rootDir cannot be null.")
    if (!rootDir.exists) throw new IllegalArgumentException("Directory does not exist: " + rootDir)

    if (includes == null) throw new IllegalArgumentException("includes cannot be null.");
    if (excludes == null) throw new IllegalArgumentException("excludes cannot be null.");
  }

  private def scanDir(dir: Directory, includes: Seq[Pattern], matches: mutable.ListBuffer[Path]) {
    if (!dir.canRead) return

    // See if patterns are specific enough to avoid scanning every file in the directory.
    val scanAll = includes exists { include =>
      (include.value.indexOf('*') != -1) || (include.value.indexOf('?') != -1)
    }

    if (!scanAll) {
      // If not scanning all the files, we know exactly which ones to include.
      for (include <- includes) {
        process(dir, dir / include.value, List(include), matches);
      }
    } else {
      // Scan every file.
      for (path <- dir.list) {
        // Get all include patterns that match.
        val matchingIncludes = includes.filter(_.matches(path.name))
        if (matchingIncludes.nonEmpty) {
          process(dir, path, matchingIncludes, matches)
        }
      }
    }
  }

  private def process(dir: Directory, path: Path, matchingIncludes: Seq[Pattern],
    matches: mutable.ListBuffer[Path]) {
    // Increment patterns that need to move to the next token.
    var isFinalMatch = false
    val incrementedPatterns = mutable.ListBuffer[Pattern]()
    val newMatchingIncludes = mutable.ListBuffer[Pattern]()

    for (include <- matchingIncludes) {
      val removeThis = if (include.incr(path.name)) {
        incrementedPatterns += include
        include.isExhausted
      } else {
        false
      }

      if (!removeThis) {
        newMatchingIncludes += include
      }

      if (include.wasFinalMatch) isFinalMatch = true
    }

    if (isFinalMatch) {
      matches += path
    }

    if (newMatchingIncludes.nonEmpty && path.isDirectory) {
      scanDir(path.toDirectory, newMatchingIncludes, matches)
    }

    // Decrement patterns.
    incrementedPatterns foreach { _.decr }
  }

  def scan(fileOnly: Boolean): Seq[Path] = {
    val includePatterns = (includes match {
      case List() => List("**")
      case _ => includes
    }).map { new Pattern(_) }
    val excludePatterns = excludes.map(new Pattern(_))

    val matches = mutable.ListBuffer[Path]()
    scanDir(rootDir, includePatterns, matches)

    // Shortcut for excludes that are "**/XXX", just check file name.
    val (shortcutExcludePatterns, restExcludePatterns) = excludePatterns span { exclude =>
      (exclude.values.length == 2) && (exclude.values.head == "**")
    }

    matches filter { path =>
      !fileOnly || path.isFile
    } filterNot { path =>
      val shouldExclude = checkIfShouldExclude(path, shortcutExcludePatterns, restExcludePatterns)
      excludePatterns foreach { _.reset }
      shouldExclude
    }
  }

  private def checkIfShouldExclude(path: Path, shortcutExcludePatterns: Seq[Pattern],
    restExcludePatterns: Seq[Pattern]): Boolean = {

    val shortcutMatch = shortcutExcludePatterns exists { e =>
      e.incr
      e.matches(path.name)
    }
    if (shortcutMatch) return true

    val parts = rootDir.relativize(path).segments
    val restMatch = restExcludePatterns exists { e =>
      var matchFlag = false
      var loop = true
      val partIter = parts.iterator
      while (loop && !matchFlag && partIter.hasNext) {
        val part = partIter.next
        if (!e.matches(part)) {
          loop = false
        } else {
          e.incr(part)
          if (e.wasFinalMatch) {
            matchFlag = true
          }
        }
      }

      matchFlag
    }

    restMatch
  }
}

object GlobScanner {
  def apply(dir: Directory,
    includes: Seq[String] = Seq(),
    excludes: Seq[String] = Seq(),
    fileOnly: Boolean = false) =
    new GlobScanner(dir, includes, excludes).scan(fileOnly)
}
