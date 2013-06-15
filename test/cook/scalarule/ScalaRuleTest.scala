package cook.scalarule

import cook.config.ConfigCompiler
import cook.config.ConfigEngine
import cook.config.ConfigGenerator
import cook.config.ConfigLoader
import cook.config.ConfigRef
import cook.config.testing.ConfigRefTestHelper
import cook.path.testing.PathUtilHelper
import cook.util.testing.ClassPathBuilderHelper

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.reflect.io.{ Path => SPath, Directory }

class ScalaRuleTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  before {
    fakePath("test1")
    ClassPathBuilderHelper.reset(ConfigCompiler.cpBuilder)
    ConfigCompiler.initDefaultCp
    ConfigEngine.init
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

  "ScalaRule" should "work" in {
    val r = ConfigRef(List("COOK"))
    val c = ConfigEngine.load(r)
  }
}
