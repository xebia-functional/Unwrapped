package continuations

import dotty.tools.dotc.ast.Trees.Block
import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class CallsSuspendParameterSuite extends FunSuite, CompilerFixtures {

  continutationsContextAndMethodCallWithSuspend.test(
    "should return Some(tree) when the tree is a method call with `given Suspend`") {
    case (given Context, tree) =>
      val call = tree match
        case Block(_, call) => call

      val prev = call.show
      val _ = CallsSuspendParameter(call)
      assertNoDiff(call.show, prev)
  }

  continutationsContextAndMethodCallWithoutSuspend.test(
    "should return None when the tree is a method call without `given Suspend`") {
    case (given Context, tree) =>
      val call = tree match
        case Block(_, call) => call

      assertEquals(CallsSuspendParameter(call), false)
  }

  continuationsContextAndZeroAritySuspendSuspendingDefDef.test(
    "should return None when the tree is not a method call") {
    case (given Context, tree) =>
      assertEquals(HasSuspensionNotInReturnedValue.unapply(tree), None)
  }
}
