package continuations

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

import TreeOps.*

class SuspensionPointsSuite extends FunSuite, CompilerFixtures {

  continutationsContextAndSuspendingSingleArityWithDependentNonSuspendingCaclulation.test(
    "It should extract the val y valdef suspension point from the tree") {
    case (given Context, tree) =>
      val obtained = SuspensionPoints.unapplySeq(tree).get
      assert(obtained.size == 1 && obtained.exists(_.existsSubTree {
        case t => t.symbol.name.show == "y"
      }))
  }

  continutationsContextAndInlinedSuspendingSingleArityWithDependentNonSuspendingCaclulation.test(
    "It should extract the val y valdef suspension point from the tree while inlined") {
    case (given Context, tree) =>
      val obtained = SuspensionPoints.unapplySeq(tree).get
      assert(obtained.size == 1 && obtained.exists(_.existsSubTree {
        case t => t.symbol.name.show == "y"
      }))
  }

  continuationsContextAndZeroAritySuspendSuspendingDefDef.test(
    "It should return none when the suspension point is not in a defdef or valdef"
  ) {
    case (given Context, tree) =>
      assertEquals(SuspensionPoints.unapplySeq(tree), None)
  }




}
