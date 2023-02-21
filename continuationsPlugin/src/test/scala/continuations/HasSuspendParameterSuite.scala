package continuations

import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class HasSuspendParameterSuite extends FunSuite, CompilerFixtures {

  continuationsContextAndZeroAritySuspendNonSuspendingDefDef.test(
    "#unapply(defDefTree): def mySuspend()(using Suspend): Int = 1 should be Some(tree)") {
    case (given Context, defdef) =>
      assertNoDiff(HasSuspendParameter.unapply(defdef).get.show, defdef.show)
  }

  continuationsContextAndZeroArityNonSuspendNonSuspendingDefDef.test(
    "#unapply(defDefTree): def mySuspend(s: Suspend): Int = 1 should be None") {
    case (given Context, defdef) =>
      assertEquals(HasSuspendParameter.unapply(defdef), None)
  }

  continuationsContextAndZeroAritySuspendNonSuspendingValDef.test(
    "#unapply(valDefTree): def mySuspend: Suspend ?=> Int = 1 should be None") {
    case (given Context, valdef) =>
      assertEquals(HasSuspendParameter.unapply(valdef), None)
  }
}
