package continuations

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Types.Type
import munit.FunSuite

class IsSuspendContextFunctionSuite extends FunSuite, CompilerFixtures {

  continuationsContextAndSuspendContextFunction.test(
    "It should detect context functions with Suspend as a parameter") {
    case (c, suspendContextFunction) =>
      given Context = c
      val t = suspendContextFunction
      assertEquals(IsSuspendContextFunction.unapply(t), Some(t))
  }

  continuationsContextAndSuspendContextFunctionReturningSuspend.test(
    "It should detect if suspend is a context function is a parameter and a return type") {
    case (c, suspendContextFunctionReturningSuspend) =>
      given Context = c
      val t = suspendContextFunctionReturningSuspend
      assertEquals(IsSuspendContextFunction.unapply(t), Some(t))
  }

  continuationsContextAndNonSuspendContextFunctionReturingSuspend.test(
    "It should not detect context functions with Suspend as a return if Suspend is not a parameter") {
    case (c, nonSuspendContextFunctionReturingSuspend) =>
      given Context = c
      assertEquals(
        IsSuspendContextFunction.unapply(nonSuspendContextFunctionReturingSuspend),
        None)
  }

  continuationsContextAndNonSuspendContextFunction.test(
    "It should not detect context functions without Suspend as a parameter") {
    case (c, nonSuspendContextFunction) =>
      given Context = c
      assertEquals(IsSuspendContextFunction.unapply(nonSuspendContextFunction), None)
  }
}
