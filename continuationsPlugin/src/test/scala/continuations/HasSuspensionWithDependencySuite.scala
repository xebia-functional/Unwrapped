package continuations

import munit.FunSuite
import dotty.tools.dotc.core.Contexts.Context

class HasSuspensionWithDependencySuite extends FunSuite, CompilerFixtures {

  continutationsContextAndSuspendingSingleArityWithDependentNonSuspendingCaclulation.test(
    "should return Some(tree) when the tree has a continuation and a continuation dependent calculation") {
    case (given Context, tree) =>
      assertNoDiff(HasSuspensionWithDependency.unapply(tree).get.show, Some(tree).get.show)
  }

  continutationsContextAndInlinedSuspendingSingleArityWithDependentNonSuspendingCaclulation
    .test(
      "should return Some(tree) when the tree has a continuation and a continuation dependent calculation when the suspend call is inlined") {
      case (given Context, tree) =>
        assertNoDiff(HasSuspensionWithDependency.unapply(tree).get.show, Some(tree).get.show)
    }

  continuationsContextAndZeroAritySuspendSuspendingDefDef.test("should return None when the tree has a continuation and no dependent caluculations"){
    case (given Context, tree) =>
        assertEquals(HasSuspensionWithDependency.unapply(tree), None)
  }

}
