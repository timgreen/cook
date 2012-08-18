package cook.config.dsl

import cook.config.Config
import cook.config.ConfigCompiler
import cook.config.ConfigGenerator
import cook.config.ConfigLoader
import cook.config.ConfigEngine
import cook.config.ConfigRef
import cook.config.testing.ConfigRefTestHelper
import cook.path.testing.PathUtilHelper
import cook.util.testing.ClassPathBuilderHelper

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


class BuildinTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

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

  "Glob" should "scan current dir" in {
    fakePath("test1")

    val r = ConfigRef(List("a", "b", "c", "COOK"))
    val s = ConfigRef(List("rules", "string.cooki"))

    val c = ConfigEngine.load(r).asInstanceOf[{
      def a: List[Path]
      def b: List[Path]
      def c: List[Path]
    }]

    c.a map { _.name } should be (List("aaa", "abc"))
    c.b map { _.name } should be (List("bbb", "abc"))
    c.c map { _.name } should be (List("abc"))
  }

  it should "support excludes" in {
    fakePath("test1")

    val r = ConfigRef(List("a", "b", "c", "COOK"))
    val s = ConfigRef(List("rules", "string.cooki"))

    val c = ConfigEngine.load(r).asInstanceOf[{
      def a2: List[Path]
      def b2: List[Path]
    }]

    c.a2 map { _.name } should be (List("abc"))
    c.b2 map { _.name } should be (List("bbb"))
  }
}
