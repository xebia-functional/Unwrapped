package continuations

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Symbols.defn
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

  compilerContextWithContinuationsPlugin.test(
    "It should detect complicate context functions with Suspend as an implicit parameter") {
    case given Context =>
      val suspend = Symbols.requiredClassRef("continuations.Suspend")

      val type1 = defn.FunctionOf(List(defn.IntType), defn.IntType)
      val type2 = defn.FunctionOf(List(defn.BooleanType, suspend), type1, isContextual = true)
      val type3 = defn.FunctionOf(List(defn.IntType), type2)
      val type4 = defn.FunctionOf(List(defn.StringType), type3, isContextual = true)
      val type5 = defn.FunctionOf(List(defn.IntType), type4)

      assertEquals(IsSuspendContextFunction.unapply(type5), Some(type5))
  }
}
