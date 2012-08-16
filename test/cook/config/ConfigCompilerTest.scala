package cook.config

import cook.config.testing.ConfigRefTestHelper
import cook.path.testing.PathUtilHelper
import cook.util.testing.ClassPathBuilderHelper


import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


class ConfigCompilerTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

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

  "Compiler" should "generate bytecodes" in {
    fakePath("test1")
    ConfigGenerator.generate(ConfigRef.rootConfigRef)
    ConfigCompiler.compile(ConfigRef.rootConfigRef)

    val r = ConfigRef(List("COOK"))
    val a = ConfigRef(List("rules", "a.cooki"))
    ConfigGenerator.generate(r)
    ConfigGenerator.generate(a)
    ConfigCompiler.compile(a)
    ConfigCompiler.compile(r)
  }
}
