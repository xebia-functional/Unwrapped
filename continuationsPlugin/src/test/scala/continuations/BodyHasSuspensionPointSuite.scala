package continuations

import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class BodyHasSuspensionPointSuite extends FunSuite, CompilerFixtures:

  continuationsContextAndZeroArityNonSuspendNonSuspendingDefDef.test(
    """|BodyHasNoSuspensionPoint#unapply(defDefTree):
       |def mySuspend()(using Suspend): Int = 1
       |should be None""".stripMargin) {
    case (given Context, defdef) =>
      assertEquals(BodyHasSuspensionPoint.unapply(defdef), None)
  }

  continuationsContextAndZeroAritySuspendSuspendingDefDef.test(
    """|BodyHasNoSuspensionPoint#unapply(defDefTree):
       |def mySuspend()(using Suspend): Int =
       |  summon[Suspend].suspendContinuation[Int] {continuation =>
       |    continuation.resume(Right(1))
       |  }
       |should be some(mySuspend)""".stripMargin) {
    case (given Context, defdef) =>
      val d = defdef
      assertEquals(BodyHasSuspensionPoint.unapply(d), Some(d))
  }

  continuationsContextAndZeroAritySuspendNonSuspendingDefDef.test(
    """|BodyHasNoSuspensionPoint#unapply(defDefTree):
       |def mySuspend()(using Suspend): Int = 1
       |should be None""".stripMargin) {
    case (ctx, defdef) =>
      given Context = ctx
      assertEquals(BodyHasSuspensionPoint.unapply(defdef), None)
  }
