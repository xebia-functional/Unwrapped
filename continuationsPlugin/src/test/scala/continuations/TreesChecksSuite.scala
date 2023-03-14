package continuations

import dotty.tools.dotc.ast.Trees.{Apply, Block, DefDef, Inlined}
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class TreesChecksSuite extends FunSuite, CompilerFixtures, TreesChecks {

  continuationsContextAndInlinedSuspendingTree.test(
    """|subtreeCallsSuspend(Suspend#shift[Int] { continuation =>
       |  continuation.resume(1)
       |})
       | should be true""".stripMargin) {
    case (given Context, inlinedSuspend) =>
      assert(subtreeCallsSuspend(inlinedSuspend))
  }

  continuationsContextAndOneTree.test("subtreeCallsSuspend(1) should be false".stripMargin) {
    case (given Context, nonInlinedTree) =>
      assert(!subtreeCallsSuspend(nonInlinedTree))
  }

  continuationsContextAndInlinedSuspendingTree.test(
    """|treeCallsSuspend(Suspend#shift[Int] {continuation =>
       |  continuation.resume(1)
       |})
       | should be true""".stripMargin) {
    case (given Context, inlinedSuspend) =>
      assert(treeCallsSuspend(inlinedSuspend))
  }

  continuationsContextAndOneTree.test("treeCallsSuspend(1) should be false".stripMargin) {
    case (given Context, nonInlinedTree) =>
      assert(!treeCallsSuspend(nonInlinedTree))
  }

  continuationsContextAndInlinedSuspendingTree.test(
    "treeCallsResume(continuation.resume(1)) should be true".ignore) {
    case (given Context, inlinedSuspend) =>
      val resume = inlinedSuspend match
        case Inlined(
              Apply(_, List(Block(_, Block(List(DefDef(_, _, _, Block(_, resumeCall))), _)))),
              _,
              _) =>
          resumeCall
        case _ => tpd.EmptyTree

      assert(treeCallsResume(resume))
  }

  continuationsContextAndInlinedSuspendingTree.test(
    """
      |treeCallsResume(Suspend#shift[Int] {continuation =>
      |  continuation.resume(1)
      |})
      | should be false
      |""".stripMargin) {
    case (given Context, inlinedSuspend) =>
      assert(!treeCallsResume(inlinedSuspend))
  }

  continutationsContextAndInlinedSuspendingSingleArityWithDependentNonSuspendingCalculation
    .test("""|valDefTreeCallsSuspend(val y = Suspend#shift[Int] {continuation =>
             |  continuation.resume(x+1)
             |})
             | should be true""".stripMargin) {
      case (given Context, tree) =>
        val suspendVal = tree.filterSubTrees {
          case vd: tpd.ValDef if vd.name.show == "y" => true
          case _ => false
        }.head
        assert(valDefTreeCallsSuspend(suspendVal))
    }

  continuationsContextAndInlinedSuspendingTree.test(
    """|valDefTreeCallsSuspend(Suspend#shift[Int] {continuation =>
       |  continuation.resume(1)
       |})
       | should be false""".stripMargin) {
    case (given Context, tree) =>
      assert(!valDefTreeCallsSuspend(tree))
  }
}
