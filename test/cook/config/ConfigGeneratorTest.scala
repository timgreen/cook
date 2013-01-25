package cook.config

import cook.error.CookException
import cook.path.Path
import cook.ref.FileRef
import cook.ref.RefFactoryRegister
import cook.ref.RefManager

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.io.Source
import scala.reflect.io.{ Path => SPath, Directory }


class ConfigGeneratorTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  before {
    RefFactoryRegister.init
  }

  private def chroot(dirname: String) {
    val dir = Directory("testdata/" + dirname)
    Path(Some(dir)).cookWorkspaceDir.deleteRecursively
  }

  private def generateSourceFor(refName: String) = {
    val ref = new ConfigRef(RefManager(Nil, refName).as[FileRef])
    val rootRef = new ConfigRef(ConfigRef.rootConfigFileRef)
    val depConfigRefsMap = ((ref.imports map { _.ref }) ++ ref.mixins ++ rootRef.mixins) map { r =>
      r.refName -> new ConfigRef(r)
    } toMap

    ConfigGenerator.generate(ref, rootRef, depConfigRefsMap)
    ref.configScalaSourceFile
  }

  "Generator" should "include imports" in {
    chroot("test1")
    val f = generateSourceFor("/COOK")
    val content = Source.fromFile(f.jfile).getLines().toSeq
    content should contain ("import COOK_CONFIG_PACKAGE.rules.a_cooki._")
    content should contain ("val b: COOK_CONFIG_PACKAGE.rules.b_cookiTrait = COOK_CONFIG_PACKAGE.rules.b_cooki")
  }

  it should "report error when COOK_ROOT include lines other than mixin" in {
    chroot("test2")
    evaluating {
      val f = generateSourceFor("/COOK")
    } should produce [CookException]
  }

  it should "report error when include cooki doesn't exists" in {
    chroot("test3")
    evaluating {
      val f = generateSourceFor("/COOK")
    } should produce [CookException]
  }

  it should "report error when cooki try to import others" in {
    chroot("test6")
    evaluating {
      val f = generateSourceFor("/COOK")
    } should produce [CookException]
  }

  it should "import @mixined imports" in {
    chroot("test7")
    val f = generateSourceFor("/COOK")
    val content = Source.fromFile(f.jfile).getLines().toSeq
    content should contain ("import COOK_CONFIG_PACKAGE.rules.a_cooki._")
  }
}
