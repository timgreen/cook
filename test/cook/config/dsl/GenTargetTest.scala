package cook.config.dsl

import cook.config.Config
import cook.config.ConfigCompiler
import cook.config.ConfigGenerator
import cook.config.ConfigLoader
import cook.config.ConfigRef
import cook.config.testing.ConfigRefTestHelper
import cook.path.testing.PathUtilHelper
import cook.util.testing.ClassPathBuilderHelper

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


class GenTargetTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

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

  "GenTarget" should "gen stringLength targets" in {
    fakePath("test1")

    val r = ConfigRef(List("a", "b", "c", "COOK"))
    val s = ConfigRef(List("rules", "string.cooki"))

    ConfigGenerator.generate(ConfigRef.rootConfigRef)
    ConfigGenerator.generate(r)
    ConfigGenerator.generate(s)

    ConfigCompiler.compile(s)
    ConfigCompiler.compile(ConfigRef.rootConfigRef)
    ConfigCompiler.compile(r)

    val c = ConfigLoader.load(r)
    val ts = c.targets

    ts.size should be (2)
    val t1 = ts("target_abc")
    t1.build
    t1.result.asInstanceOf[{ def length: Int}].length should be (3)
    val t2 = ts("target_defg")
    t2.build
    t2.result.asInstanceOf[{ def length: Int}].length should be (4)
  }
}
