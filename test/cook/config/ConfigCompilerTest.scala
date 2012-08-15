package cook.config

import cook.config.testing.ConfigRefTestHelper
import cook.path.testing.PathUtilHelper
import cook.util.testing.ClassPathBuilderHelper


import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.io.Source
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


class ConfigCompilerTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  before {
    ClassPathBuilderHelper.reset(ConfigCompiler.cpBuilder)
    ConfigCompiler.initDefaultCp
  }

  def generator(dirname: String) = {
    val dir = Directory("testoutput/" + dirname)
    dir.deleteRecursively
    PathUtilHelper.rakePath(
      cookRootOption = Some((Path("testdata") / dirname) toDirectory),
      cookConfigScalaSourceDirOption = Some(dir)
    )
    ConfigScalaSourceGenerator
  }

  def compiler(dirname: String) = {
    val sourceDir = Directory("testoutput/scala/" + dirname)
    val bytecodeDir = Directory("testoutput/bytecode/" + dirname)
    PathUtilHelper.rakePath(
      cookRootOption = Some((Path("testdata") / dirname) toDirectory),
      cookConfigScalaSourceDirOption = Some(sourceDir),
      cookConfigByteCodeDirOption = Some(bytecodeDir)
    )

    ConfigCompiler
  }

  "Compiler" should "generate bytecodes" in {
    val g = generator("test1")
    val c = compiler("test1")
    g.generate(ConfigRef.rootConfigRef)
    c.compile(ConfigRef.rootConfigRef)

    val r = ConfigRef(List("COOK"))
    val a = ConfigRef(List("rules", "a.cooki"))
    g.generate(r)
    g.generate(a)
    c.compile(a)
    c.compile(r)
  }
}
