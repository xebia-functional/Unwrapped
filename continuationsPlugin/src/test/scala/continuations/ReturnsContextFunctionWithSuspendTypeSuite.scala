package continuations

import dotty.tools.dotc.ast.tpd.DefDef
import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class ReturnsContextFunctionWithSuspendTypeSuite extends FunSuite, CompilerFixtures:

  continuationsContextAndZeroArityContextFunctionDefDef.test(
    "it should detect a def def that returns a suspending context function") {
    case (c, zeroArityContextFunctionDefDef) =>
      given Context = c
      val t = zeroArityContextFunctionDefDef
      assertEquals(ReturnsContextFunctionWithSuspendType.unapply(t), Some(t))
  }

  continuationsContextAndSuspendingContextFunctionValDef.test(
    "it should detect a val def that has a suspending context function") {
    case (c, suspendingContextFunctionValDef) =>
      given Context = c
      val t = suspendingContextFunctionValDef
      assertEquals(ReturnsContextFunctionWithSuspendType.unapply(t), Some(t))
  }

  continuationsContextAndNonSuspendingContextFunctionValDef.test(
    "it should not detect a val def that is not a suspending context function") {
    case (c, nonSuspendingContextFunctionValDef) =>
      given Context = c
      val t = nonSuspendingContextFunctionValDef
      assertEquals(ReturnsContextFunctionWithSuspendType.unapply(t), None)
  }
