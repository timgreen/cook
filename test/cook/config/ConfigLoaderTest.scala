package cook.config

import cook.config.testing.ConfigRefTestHelper
import cook.path.testing.PathUtilHelper
import cook.util.testing.ClassPathBuilderHelper

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


class ConfigLoaderTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

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

  "Loader" should "load class" in {
    fakePath("test1")

    val r = ConfigRef(List("COOK"))
    val a = ConfigRef(List("rules", "a.cooki"))

    ConfigGenerator.generate(ConfigRef.rootConfigRef)
    ConfigGenerator.generate(r)
    ConfigGenerator.generate(a)

    ConfigCompiler.compile(a)
    ConfigCompiler.compile(ConfigRef.rootConfigRef)
    ConfigCompiler.compile(r)

    val c = ConfigLoader.load(r)
    println(c)
  }
}
