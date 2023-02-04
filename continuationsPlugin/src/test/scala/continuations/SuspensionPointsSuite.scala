package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class SuspensionPointsSuite extends FunSuite, CompilerFixtures, Trees {

  continutationsContextAndSuspendingSingleArityWithDependentNonSuspendingCalculation.test(
    "It should extract the val y valdef suspension point from the tree") {
    case (given Context, tree) =>
      val obtained = SuspensionPoints.unapplySeq(tree).get
      assert(
        obtained.withValSize == 1 && obtained.nonValSize == 0 &&
          obtained.withVal.get.exists(_.existsSubTree { t => t.symbol.name.show == "y" }))
  }

  continutationsContextAndInlinedSuspendingSingleArityWithDependentNonSuspendingCalculation
    .test("It should extract the val y valdef suspension point from the tree while inlined") {
      case (given Context, tree) =>
        val obtained = SuspensionPoints.unapplySeq(tree).get
        assert(
          obtained.withValSize == 1 && obtained.nonValSize == 0 &&
            obtained.withVal.get.exists(_.existsSubTree { t => t.symbol.name.show == "y" }))
    }

  continuationsContextAndZeroAritySuspendSuspendingDefDef.test(
    "It should extract suspension points from the tree that are not assigned to a val"
  ) {
    case (given Context, tree) =>
      val obtained = SuspensionPoints.unapplySeq(tree).get

      assert(
        obtained.withValSize == 0 && obtained.nonValSize == 1 &&
          obtained
            .nonVal
            .get
            .exists(_.existsSubTree {
              case Inlined(call, _, _) => call.symbol.matches(suspendContinuationMethod.symbol)
              case _ => false
            }))
  }
}
