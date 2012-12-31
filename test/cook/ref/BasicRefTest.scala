package cook.ref

import cook.error.CookException

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class BasicRefTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  RefFactoryRegister.init

  "RefManager" should "recognize DirRef: \"a/b/c/\"" in {
    val ref = RefManager(Nil, "a/b/c/")
    ref.isInstanceOf[DirRef] should be (true)
    val dirRef = ref.asInstanceOf[DirRef]
    dirRef.segments should be (List("a", "b", "c"))
    dirRef.refName should be ("/a/b/c/")
  }

  it should "recognize Root DirRef: \"/\"" in {
    val dirRef = RefManager(List("a", "b", "c"), "/").asInstanceOf[DirRef]
    dirRef.segments should be (Nil)
    dirRef.refName should be ("/")
  }

  it should "recognize Root DirRef: \"./\"" in {
    val dirRef = RefManager(Nil, "./").asInstanceOf[DirRef]
    dirRef.segments should be (Nil)
    dirRef.refName should be ("/")
  }

  it should "recognize DirRef: \"../../x/y/z/\" (current dir: /a/b/c/)" in {
    val dirRef = RefManager(List("a", "b", "c"), "../../x/y/z/").asInstanceOf[DirRef]
    dirRef.segments should be (List("a", "x", "y", "z"))
    dirRef.refName should be ("/a/x/y/z/")
  }

  it should "recognize DirRef: \"../../x/./y/../../x/y/z/\" (current dir: /a/b/c/)" in {
    val dirRef = RefManager(List("a", "b", "c"), "../../x/./y/../../x/y/z/").asInstanceOf[DirRef]
    dirRef.segments should be (List("a", "x", "y", "z"))
    dirRef.refName should be ("/a/x/y/z/")
  }

  it should "recognize DirRef: \"../../x/./y/../../x/y/z/./\" (current dir: /a/b/c/)" in {
    val dirRef = RefManager(List("a", "b", "c"), "../../x/./y/../../x/y/z/./").asInstanceOf[DirRef]
    dirRef.segments should be (List("a", "x", "y", "z"))
    dirRef.refName should be ("/a/x/y/z/")
  }

  it should "report error on \"../../../\" (current dir: /a/b/)" in {
    evaluating {
      RefManager(List("a", "b"), "../../../")
    } should produce [CookException]
  }

  it should "recognize FileRef: \"rootfile\" (current dir: /)" in {
    val fileRef = RefManager(Nil, "rootfile").asInstanceOf[FileRef]
    fileRef.dir.segments should be (Nil)
    fileRef.filename should be ("rootfile")
    fileRef.refName should be ("/rootfile")
  }

  it should "recognize FileRef: \"./rootfile\" (current dir: /)" in {
    val fileRef = RefManager(Nil, "./rootfile").asInstanceOf[FileRef]
    fileRef.dir.segments should be (Nil)
    fileRef.filename should be ("rootfile")
    fileRef.refName should be ("/rootfile")
  }

  it should "recognize FileRef: \"x/y/z\" (current dir: /a/b/c/)" in {
    val fileRef = RefManager(List("a", "b", "c"), "x/y/z").asInstanceOf[FileRef]
    fileRef.dir.segments should be (List("a", "b", "c", "x", "y"))
    fileRef.filename should be ("z")
    fileRef.refName should be ("/a/b/c/x/y/z")
  }

  it should "recognize FileRef: \"../../x/./y/../../x/y/z\" (current dir: /a/b/c/)" in {
    val fileRef = RefManager(List("a", "b", "c"), "../../x/./y/../../x/y/z").asInstanceOf[FileRef]
    fileRef.dir.segments should be (List("a", "x", "y"))
    fileRef.filename should be ("z")
    fileRef.refName should be ("/a/x/y/z")
  }

  it should "recognize NativeTargetRef: \":root_target\" (current dir: /)" in {
    val targetRef = RefManager(Nil, ":root_target").asInstanceOf[NativeTargetRef]
    targetRef.dir.segments should be (Nil)
    targetRef.targetName should be ("root_target")
    targetRef.refName should be ("/:root_target")
  }

  it should "recognize NativeTargetRef: \".:root_target\" (current dir: /)" in {
    val targetRef = RefManager(Nil, ".:root_target").asInstanceOf[NativeTargetRef]
    targetRef.dir.segments should be (Nil)
    targetRef.targetName should be ("root_target")
    targetRef.refName should be ("/:root_target")
  }

  it should "recognize NativeTargetRef: \"x/y:z\" (current dir: /a/b/c/)" in {
    val targetRef = RefManager(List("a", "b", "c"), "x/y:z").asInstanceOf[NativeTargetRef]
    targetRef.dir.segments should be (List("a", "b", "c", "x", "y"))
    targetRef.targetName should be ("z")
    targetRef.refName should be ("/a/b/c/x/y:z")
  }

  it should "recognize NativeTargetRef: \"x/y/.:z\" (current dir: /a/b/c/)" in {
    val targetRef = RefManager(List("a", "b", "c"), "x/y/.:z").asInstanceOf[NativeTargetRef]
    targetRef.dir.segments should be (List("a", "b", "c", "x", "y"))
    targetRef.targetName should be ("z")
    targetRef.refName should be ("/a/b/c/x/y:z")
  }

  it should "recognize NativeTargetRef: \"../../x/./y/../../x/y:z\" (current dir: /a/b/c/)" in {
    val targetRef = RefManager(List("a", "b", "c"), "../../x/./y/../../x/y:z").asInstanceOf[NativeTargetRef]
    targetRef.dir.segments should be (List("a", "x", "y"))
    targetRef.targetName should be ("z")
    targetRef.refName should be ("/a/x/y:z")
  }

  ignore should "report error on bad refName: \"/a/b/c/.\"" in {
    evaluating {
      RefManager(List("a", "b"), "/a/b/c/.")
    } should produce [CookException]
  }

  it should "report error on bad refName: \"/a//c/\"" in {
    evaluating {
      RefManager(List("a", "b"), "/a//c/")
    } should produce [CookException]
  }

  it should "report error on bad refName: \"/a//c\"" in {
    evaluating {
      RefManager(List("a", "b"), "/a//c")
    } should produce [CookException]
  }

  it should "report error on bad refName: \"/a/b/:\"" in {
    evaluating {
      RefManager(List("a", "b"), "/a/b/:")
    } should produce [CookException]
  }

  it should "report error on bad refName: \"/a/:b/:c\"" in {
    evaluating {
      RefManager(List("a", "b"), "/a/:b/:c")
    } should produce [CookException]
  }

  it should "report error on bad refName: \"/a/b/c:d:f\"" in {
    evaluating {
      RefManager(List("a", "b"), "/a/b/c:d:f")
    } should produce [CookException]
  }
}
