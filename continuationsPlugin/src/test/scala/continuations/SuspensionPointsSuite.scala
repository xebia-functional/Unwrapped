package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class SuspensionPointsSuite extends FunSuite, CompilerFixtures, Trees {

  continutationsContextAndSuspendingSingleArityWithDependentNonSuspendingAndNonDependentCalculation
    .test(
      "It should extract the val y valdef suspension point and a suspension point that is not " +
        "assigned to a val from the tree, keeping the order") {
      case (given Context, tree) =>
        val obtained = SuspensionPoints.unapplySeq(tree).get
        assert(
          obtained.points.size == 2 &&
            obtained.points.headOption.exists { case vd: ValDef => vd.name.show == "y" } &&
            obtained.points.lastOption.exists {
              case Inlined(call, _, _) => call.symbol.matches(suspendContinuationMethod.symbol)
            })
    }

  continutationsContextAndInlinedSuspendingSingleArityWithDependentNonSuspendingCalculation
    .test("It should extract the val y valdef suspension point from the tree while inlined") {
      case (given Context, tree) =>
        val obtained = SuspensionPoints.unapplySeq(tree).get
        assert(
          obtained.points.size == 1 &&
            obtained.points.headOption.exists { case vd: ValDef => vd.name.show == "y" })
    }
}
