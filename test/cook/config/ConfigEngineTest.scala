package cook.config

import cook.config.testing.ConfigRefTestHelper
import cook.error.CookException
import cook.path.testing.PathUtilHelper
import cook.util.testing.ClassPathBuilderHelper

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


class ConfigEngineTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  before {
    ClassPathBuilderHelper.reset(ConfigCompiler.cpBuilder)
    ConfigCompiler.initDefaultCp
  }

  def fakePath(dirname: String) {
    val dir = Directory("testoutput/" + dirname)
    dir.deleteRecursively

    val sourceDir = dir / "scala"
    val bytecodeDir = dir / "bytecode"
    PathUtilHelper.fakePath(
      cookRootOption = Some((Path("testdata") / dirname) toDirectory),
      cookConfigScalaSourceDirOption = Some(sourceDir toDirectory),
      cookConfigByteCodeDirOption = Some(bytecodeDir toDirectory)
    )
  }

  "Engine" should "load config on demand" in {
    fakePath("test1")

    val r = ConfigRef(List("COOK"))

    val c = ConfigEngine.load(r)
  }

  it should "report error on bad ref name, starts with //" in {
    fakePath("test1")

    val r = ConfigRef(List("badref1", "COOK"))
    evaluating {
      val c = ConfigEngine.load(r)
    } should produce [CookException]
  }

  it should "report error on bad ref name, contains //" in {
    fakePath("test1")

    val r = ConfigRef(List("badref2", "COOK"))
    evaluating {
      val c = ConfigEngine.load(r)
    } should produce [CookException]
  }

  it should "report error on bad ref name, contains :" in {
    fakePath("test1")

    val r = ConfigRef(List("badref3", "COOK"))
    evaluating {
      val c = ConfigEngine.load(r)
    } should produce [CookException]
  }
}
