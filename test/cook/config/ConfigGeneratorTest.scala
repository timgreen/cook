package cook.config

import cook.config.testing.ConfigRefTestHelper
import cook.error.CookException
import cook.path.testing.PathUtilHelper

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.io.Source
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


class ConfigGeneratorTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  before {
    ConfigRefTestHelper.clearCache
  }

  def generator(dirname: String) = {
    val dir = Directory("testoutput/" + dirname)
    dir.deleteRecursively
    PathUtilHelper.fakePath(
      cookRootOption = Some((Path("testdata") / dirname) toDirectory),
      cookConfigScalaSourceDirOption = Some(dir)
    )
    ConfigGenerator
  }

  "Generator" should "include imports" in {
    val g = generator("test1")
    val f = g.generate(ConfigRef(List("COOK")))
    val content = Source.fromFile(f.jfile).getLines().toSeq
    content should contain ("val a: COOK_CONFIG_PACKAGE.rules.a_cookiTrait = COOK_CONFIG_PACKAGE.rules.a_cooki")
    content should contain ("import a._")
  }

  it should "report error when COOK_ROOT include lines other than import" in {
    val g = generator("test2")
    evaluating {
      g.generate(ConfigRef(List("COOK")))
    } should produce [CookException]
  }

  it should "report error when include cooki doesn't exists" in {
    val g = generator("test3")
    evaluating {
      val f = g.generate(ConfigRef(List("COOK")))
    } should produce [CookException]
  }

  it should "report error when detect cycle include" in {
    val g = generator("test4")
    evaluating {
      val f = g.generate(ConfigRef(List("COOK")))
    } should produce [CookException]
  }

  it should "report error when detect self include" in {
    val g = generator("test5")
    evaluating {
      val f = g.generate(ConfigRef(List("COOK")))
    } should produce [CookException]
  }

  it should "mixin imports for COOK_ROOT" in {
    val g = generator("test6")
    val f = g.generate(ConfigRef.rootConfigRef)
    val content = Source.fromFile(f.jfile).getLines().toSeq
    content should contain ("  with COOK_CONFIG_PACKAGE.rules.a_cookiTrait")
    content should contain ("  with COOK_CONFIG_PACKAGE.rules.b_cookiTrait")
  }

  it should "report error when @import @mixined files" in {
    val g = generator("test7")
    evaluating {
      val f = g.generate(ConfigRef(List("COOK")))
    } should produce [CookException]
  }

  it should "change import object when include in COOK_ROOT mixin" in {
    val g = generator("test8")
    val f = g.generate(ConfigRef(List("COOK")))
    val content = Source.fromFile(f.jfile).getLines().toSeq
    content should contain ("val a: COOK_CONFIG_PACKAGE.rules.a_cookiTrait = COOK_CONFIG_PACKAGE.COOK_ROOT")
  }
}