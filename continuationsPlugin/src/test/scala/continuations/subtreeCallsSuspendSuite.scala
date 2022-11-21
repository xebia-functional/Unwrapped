package continuations

import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class subtreeCallsSuspendSuite extends FunSuite, CompilerFixtures {

  continuationsContextAndInlinedSuspendingTree.test(
    """|subtreeCallsSuspend(Continuation.suspendContinuation[Int] {continuation =>
       |  continuation.resume(Right(1))
       |})
       | should be true""".stripMargin) {
    case (given Context, inlinedSuspend) =>
      assert(subtreeCallsSuspend(inlinedSuspend))
  }

  continuationsContextAndOneTree.test(
    """|subtreeCallsSuspend(1) should be false""".stripMargin) {
    case (given Context, nonInlinedTree) =>
      assert(!subtreeCallsSuspend(nonInlinedTree))
  }
}
