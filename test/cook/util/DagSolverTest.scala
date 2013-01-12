package cook.util

import cook.util.DagSolver.FoundDepCycle
import cook.util.DagSolver.Ok

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class DagSolverTest extends FlatSpec with ShouldMatchers {

  "Dag Solver" should "enable to find topological sorting" in {
    val solver = new DagSolver();
    solver.hasAvaliable should be (false)
    solver.addDeps("a", "b" :: "c" :: "d" :: Nil) should be (Ok)
    solver.hasAvaliable should be (false)
    solver.avaliableNodesSet should be (Set())
    solver.addDeps("b", "c" :: "d" :: Nil) should be (Ok)
    solver.hasAvaliable should be (false)
    solver.avaliableNodesSet should be (Set())
    solver.addDeps("c", "d" :: Nil) should be (Ok)
    solver.hasAvaliable should be (false)
    solver.avaliableNodesSet should be (Set())
    solver.addDeps("d", Nil) should be (Ok)
    solver.hasAvaliable should be (true)
    solver.avaliableNodesSet should be (Set("d"))
    solver.pop should be ("d")
    solver.hasAvaliable should be (false)
    solver.markDone("d")
    solver.hasAvaliable should be (true)
    solver.avaliableNodesSet should be (Set("c"))
    solver.pop should be ("c")
    solver.hasAvaliable should be (false)
    solver.markDone("c")
    solver.hasAvaliable should be (true)
    solver.avaliableNodesSet should be (Set("b"))
    solver.pop should be ("b")
    solver.hasAvaliable should be (false)
    solver.markDone("b")
    solver.hasAvaliable should be (true)
    solver.avaliableNodesSet should be (Set("a"))
    solver.pop should be ("a")
    solver.hasAvaliable should be (false)
    solver.markDone("a")
    solver.hasAvaliable should be (false)
    solver.avaliableNodesSet should be (Set())
  }

  it should "enable to find cycle" in {
    val solver = new DagSolver();
    solver.addDeps("a", "b" :: Nil) should be (Ok)
    solver.addDeps("b", "c" :: Nil) should be (Ok)
    solver.addDeps("c", "d" :: Nil) should be (Ok)
    solver.addDeps("d", "a" :: Nil) should be (FoundDepCycle(List("c", "b", "a", "d")))
  }
}
