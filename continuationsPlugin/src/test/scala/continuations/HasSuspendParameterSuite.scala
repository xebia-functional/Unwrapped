package continuations

import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class HasSuspendParameterSuite extends FunSuite, CompilerFixtures {

  continuationsContextAndZeroAritySuspendNonSuspendingDefDef.test(
    "#apply(defDefTree): def mySuspend()(using Suspend): Int = 1 should be Some(tree)") {
    case (given Context, defdef) =>
      assertEquals(HasSuspendParameter(defdef), true)
  }

  continuationsContextAndZeroArityNonSuspendNonSuspendingDefDef.test(
    "#apply(defDefTree): def mySuspend(s: Suspend): Int = 1 should be None") {
    case (given Context, defdef) =>
      assertEquals(HasSuspendParameter(defdef), false)
  }

  continuationsContextAndZeroAritySuspendNonSuspendingValDef.test(
    "#apply(valDefTree): def mySuspend: Suspend ?=> Int = 1 should be None") {
    case (given Context, valdef) =>
      assertEquals(HasSuspendParameter(valdef), false)
  }
}
