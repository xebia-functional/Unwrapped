package continuations

import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class HasSuspendParameterSuite extends FunSuite, CompilerFixtures {

  continuationsContextAndZeroAritySuspendNonSuspendingDefDef.test(
    "#unapply(defDefTree): def mySuspend()(using Suspend): Int = 1 should be Some(tree)") {
    case (context, defdef) =>
      given Context = context
      val d = defdef
      assertEquals(HasSuspendParameter.unapply(d), Some(d))
  }

  continuationsContextAndZeroArityNonSuspendNonSuspendingDefDef.test(
    "#unapply(defDefTree): def mySuspend(s: Suspend): Int = 1 should be None") {
    case (context, defdef) =>
      given Context = context
      val d = defdef
      assertEquals(HasSuspendParameter.unapply(d), None)
  }
}
