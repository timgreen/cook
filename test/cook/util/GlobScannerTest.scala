package cook.util

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path

class GlobScannerTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  val testRoot = Path("testdata")

  def comparePathSet(results: Seq[Path], excepts: Seq[Path]) {
    results should have length (excepts.length)
    for (p <- excepts) {
      results should contain (p)
    }
  }

  "Pattern" should "normalize input pattern" in {
    val p = new Pattern("a**b")
    p.values should be (Array("a*", "**", "*b"))
  }

  it should "?? should match ab" in {
    val p = new Pattern("??")
    p.matches("ab") should be (true)
  }

  it should "?? should not match a" in {
    val p = new Pattern("??")
    p.matches("a") should be (false)
  }

  it should "a*a*a?? should match asdfasdfasd" in {
    val p = new Pattern("a*a*a*??")
    p.matches("asdfasdfasd") should be (true)
  }

  it should "a*a*a?? should not match asdfasdfasdf" in {
    val p = new Pattern("a*a*a*??")
    p.matches("asdfasdfasdf") should be (false)
  }

  "Glob Scanner" should "throw java.lang.IllegalArgumentException when dir not exist" in {
    val root = (testRoot / "not_exist").toDirectory
    evaluating {
      GlobScanner(root)
    } should produce [IllegalArgumentException]
  }

  it should "throw java.lang.IllegalArgumentException when dir is null" in {
    evaluating {
      GlobScanner(null)
    } should produce [IllegalArgumentException]
  }

  it should "return all items in the dir by default" in {
    val root = (testRoot / "test_scan_all").toDirectory
    val result = GlobScanner(root)
    val except = List(
      root / "a",
      root / "b",
      root / "c",
      root / "d",
      root / "d" / "e",
      root / "d" / "e" / "f",
      root / "d" / "e" / "f" / "g",
      root / "d" / "e" / "f" / "h",
      root / "d" / "e" / "f" / "j",
      root / "d" / "e" / "f" / "j" / "k",
      root / "d" / "e" / "f" / "j" / "k" / "1",
      root / "d" / "e" / "f" / "j" / "k" / "2"
    )
    comparePathSet(result, except)
  }

  it should "return all files in the dir when fileOnly = true" in {
    val root = (testRoot / "test_scan_all").toDirectory
    val result = GlobScanner(root, fileOnly = true)
    val except = List(
      root / "a",
      root / "b",
      root / "c",
      root / "d" / "e" / "f" / "g",
      root / "d" / "e" / "f" / "h",
      root / "d" / "e" / "f" / "j" / "k" / "1",
      root / "d" / "e" / "f" / "j" / "k" / "2"
    )
    comparePathSet(result, except)
  }

  it should "'d/?/f' should match 'd/e/f'" in {
    val root = (testRoot / "test_scan_all").toDirectory
    val result = GlobScanner(root, Seq("d/?/f"))
    val except = List(
      root / "d" / "e" / "f"
    )
    comparePathSet(result, except)
  }

  it should "'d/?/f/?' should match 3 items" in {
    val root = (testRoot / "test_scan_all").toDirectory
    val result = GlobScanner(root, Seq("d/?/f/?"))
    val except = List(
      root / "d" / "e" / "f" / "g",
      root / "d" / "e" / "f" / "h",
      root / "d" / "e" / "f" / "j"
    )
    comparePathSet(result, except)
  }

  it should "'d/?/f/**' should match 7 items" in {
    val root = (testRoot / "test_scan_all").toDirectory
    val result = GlobScanner(root, Seq("d/?/f/**"))
    val except = List(
      root / "d" / "e" / "f",
      root / "d" / "e" / "f" / "g",
      root / "d" / "e" / "f" / "h",
      root / "d" / "e" / "f" / "j",
      root / "d" / "e" / "f" / "j" / "k",
      root / "d" / "e" / "f" / "j" / "k" / "1",
      root / "d" / "e" / "f" / "j" / "k" / "2"
    )
    comparePathSet(result, except)
  }

  it should "'a/*/*' should match 'a/b/c'" in {
    val root = (testRoot / "test_a").toDirectory
    val result = GlobScanner(root, Seq("a/*/*"))
    val except = List(
      root / "a" / "b" / "c"
    )
    comparePathSet(result, except)
  }

  it should "'a/**/d' should match 'a/b/c/d'" in {
    val root = (testRoot / "test_b").toDirectory
    val result = GlobScanner(root, Seq("a/**/d"))
    val except = List(
      root / "a" / "b" / "c" / "d"
    )
    comparePathSet(result, except)
  }

  it should "'a/*/d' should not match 'a/b/c/d'" in {
    val root = (testRoot / "test_b").toDirectory
    val result = GlobScanner(root, Seq("a/*/d"))
    val except = List()
    comparePathSet(result, except)
  }

  it should "'?' should match 'a'" in {
    val root = (testRoot / "test_b").toDirectory
    val result = GlobScanner(root, Seq("?"))
    val except = List(
      root / "a"
    )
    comparePathSet(result, except)
  }

  it should "'?/?' should match 'a/b'" in {
    val root = (testRoot / "test_b").toDirectory
    val result = GlobScanner(root, Seq("?/?"))
    val except = List(
      root / "a" / "b"
    )
    comparePathSet(result, except)
  }

  it should "'**/?' should match 'a'" in {
    val root = (testRoot / "test_b").toDirectory
    val result = GlobScanner(root, Seq("**/?"))
    val except = List(
      root / "a",
      root / "a" / "b",
      root / "a" / "b" / "c",
      root / "a" / "b" / "c" / "d"
    )
    comparePathSet(result, except)
  }

  it should "'**/d' should match 'a/b/c/d'" in {
    val root = (testRoot / "test_b").toDirectory
    val result = GlobScanner(root, Seq("**/d"))
    val except = List(
      root / "a" / "b" / "c" / "d"
    )
    comparePathSet(result, except)
  }

  it should "'**/??' should not match 'a/b/c/d'" in {
    val root = (testRoot / "test_b").toDirectory
    val result = GlobScanner(root, Seq("**/?"))
    val except = List()
    comparePathSet(result, except)
  }

  it should "'dir1/as*' should match 4 items" in {
    val root = (testRoot / "test_c").toDirectory
    val result = GlobScanner(root, Seq("dir1/as*"))
    val except = List(
      root / "dir1" / "asdf1",
      root / "dir1" / "asdf2",
      root / "dir1" / "asdfasdf3",
      root / "dir1" / "asdfasdfasdfasdfasdf4"
    )
    comparePathSet(result, except)
  }

  it should "'*1/as*' should match 4 items" in {
    val root = (testRoot / "test_c").toDirectory
    val result = GlobScanner(root, Seq("*1/as*"))
    val except = List(
      root / "dir1" / "asdf1",
      root / "dir1" / "asdf2",
      root / "dir1" / "asdfasdf3",
      root / "dir1" / "asdfasdfasdfasdfasdf4"
    )
    comparePathSet(result, except)
  }

  it should "'**/as*' should match 8 items" in {
    val root = (testRoot / "test_c").toDirectory
    val result = GlobScanner(root, Seq("**/as*"))
    val except = List(
      root / "dir1" / "asdf1",
      root / "dir1" / "asdf2",
      root / "dir1" / "asdfasdf3",
      root / "dir1" / "asdfasdfasdfasdfasdf4",
      root / "dir02" / "asdf1",
      root / "dir02" / "asdf2",
      root / "dir02" / "asdfasdf3",
      root / "dir02" / "asdfasdfasdfasdfasdf4"
    )
    comparePathSet(result, except)
  }
}
