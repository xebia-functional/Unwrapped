package continuations

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Types.Type
import munit.FunSuite

class IsSuspendContextualMethodSuite extends FunSuite, CompilerFixtures {

  continuationsContextAndSuspendContextualMethodDefDef.test(
    "It should detect contextual methods with Suspend as a parameter") {
    case (given Context, tree) =>
      assertEquals(IsSuspendContextualMethod.unapply(tree).get.show, tree.show)
  }

  continuationsContextAndNotSuspendContextualMethodDefDef.test(
    "It should return None when the tree is a contextual method without Suspend as a parameter") {
    case (given Context, tree) =>
      assertEquals(IsSuspendContextualMethod.unapply(tree), None)
  }

  continuationsContextAndZeroArityContextFunctionDefDef.test(
    "It should return None when the tree is not a contextual method") {
    case (given Context, tree) =>
      assertEquals(IsSuspendContextualMethod.unapply(tree), None)
  }
}
