package continuations

import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class BodyHasSuspensionPointSuite extends FunSuite, CompilerFixtures:

  continuationsContextAndZeroArityNonSuspendNonSuspendingDefDef.test(
    """|BodyHasNoSuspensionPoint#apply(defDefTree):
       |def mySuspend()(using Suspend): Int = 1
       |should be None""".stripMargin) {
    case (given Context, defdef) =>
      assertEquals(BodyHasSuspensionPoint(defdef), false)
  }

  continuationsContextAndZeroAritySuspendSuspendingDefDef.test(
    """|BodyHasNoSuspensionPoint#apply(defDefTree):
       |def mySuspend()(using Suspend): Int =
       |  summon[Suspend].suspendContinuation[Int] {continuation =>
       |    continuation.resume(Right(1))
       |  }
       |should be some(mySuspend)""".stripMargin) {
    case (given Context, defdef) =>
      val d = defdef
      assertEquals(BodyHasSuspensionPoint(d), true)
  }

  continuationsContextAndZeroAritySuspendNonSuspendingDefDef.test(
    """|BodyHasNoSuspensionPoint#apply(defDefTree):
       |def mySuspend()(using Suspend): Int = 1
       |should be None""".stripMargin) {
    case (ctx, defdef) =>
      given Context = ctx
      assertEquals(BodyHasSuspensionPoint(defdef), false)
  }
