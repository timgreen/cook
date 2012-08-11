package cook.config

import cook.path.testing.PathUtilHelper

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.io.Source
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path


class ConfigScalaSourceGeneratorTest extends FlatSpec with ShouldMatchers {

  def generator(dirname: String) = {
    PathUtilHelper.changeCookRoot((Path("testdata") / dirname) toDirectory)
    val dir = Directory("testoutput")
    dir.deleteRecursively
    new ConfigScalaSourceGenerator(dir)
  }

  "Generator" should "output to right dir" in {
    val g = generator("test1")
    val f = g.generate(ConfigRef(List("COOK")))
    val content = Source.fromFile(f.jfile).getLines().toSeq
    content should contain ("import COOK_CONFIG_PACKAGE.rules.a_cooki._")
  }
}
