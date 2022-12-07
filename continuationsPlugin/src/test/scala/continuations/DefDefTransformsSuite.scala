package continuations

import dotty.tools.dotc.ast.tpd.DefDef
import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class DefDefTransformsSuite extends FunSuite, CompilerFixtures:

  continutationsContextAndInlinedSuspendingSingleArityWithDependentNonSuspendingCaclulation
    .test("""|DefDefTransforms.countContinuationSynthetics should return the
             |number of suspension points + 6 when given a single
             |suspending defdef with one dependency and 0 for old count""".stripMargin) {
      case (given Context, tree) =>
        assertEquals(
          DefDefTransforms.countContinuationSynthetics(tree.asInstanceOf[DefDef], 0),
          8)
    }

  continuationsContextAndZeroAritySuspendNonSuspendingDefDef.test(
    """|DefDefTransforms.countContinuationSynthetics should return the 0
       |when given a non suspending suspend def and 0 for old count""".stripMargin) {
    case (given Context, tree) =>
      assertEquals(
        DefDefTransforms.countContinuationSynthetics(tree.asInstanceOf[DefDef], 0),
        0)
  }

  continuationsContextAndZeroArityNonSuspendNonSuspendingDefDef.test(
    """|DefDefTransforms.countContinuationSynthetics should return the 0
       |when given a non suspend def and 0 for old count""".stripMargin) {
    case (given Context, tree) =>
      assertEquals(
        DefDefTransforms.countContinuationSynthetics(tree.asInstanceOf[DefDef], 0),
        0)
  }

  continutationsContextAndInlinedSuspendingSingleArityWithDependentNonSuspendingCaclulation
    .test("""|DefDefTransforms.countContinuationSynthetics should return the
             |number of suspension points + 6 + 1 when given a single
             |suspending defdef with one dependency and 1 for old
             |count""".stripMargin) {
      case (given Context, tree) =>
        assertEquals(
          DefDefTransforms.countContinuationSynthetics(tree.asInstanceOf[DefDef], 1),
          9)
    }

  continuationsContextAndZeroAritySuspendNonSuspendingDefDef.test(
    """|DefDefTransforms.countContinuationSynthetics should return 1 when
       |given a non suspending suspend def and 1 for old count""".stripMargin) {
    case (given Context, tree) =>
      assertEquals(
        DefDefTransforms.countContinuationSynthetics(tree.asInstanceOf[DefDef], 1),
        1)
  }

  continuationsContextAndZeroArityNonSuspendNonSuspendingDefDef.test(
    """|DefDefTransforms.countContinuationSynthetics should return 1 when
       |given a non suspend def and 1 for old count""".stripMargin) {
    case (given Context, tree) =>
      assertEquals(
        DefDefTransforms.countContinuationSynthetics(tree.asInstanceOf[DefDef], 1),
        1)
  }

  continutationsContextAndInlinedSuspendingSingleArityWithDependentNonSuspendingCaclulation
    .test("""|DefDefTransforms.countContinuationSynthetics should return the
             |number of suspension points + 6 + 2 when given a single
             |suspending defdef with one dependency and 2 for old
             |count""".stripMargin) {
      case (given Context, tree) =>
        assertEquals(
          DefDefTransforms.countContinuationSynthetics(tree.asInstanceOf[DefDef], 2),
          10)
    }

  continuationsContextAndZeroAritySuspendNonSuspendingDefDef.test(
    """|DefDefTransforms.countContinuationSynthetics should return 2 when
       |given a non suspending suspend def and 2 for old count""".stripMargin) {
    case (given Context, tree) =>
      assertEquals(
        DefDefTransforms.countContinuationSynthetics(tree.asInstanceOf[DefDef], 2),
        2)
  }

  continuationsContextAndZeroArityNonSuspendNonSuspendingDefDef.test(
    """|DefDefTransforms.countContinuationSynthetics should return 2 when
       |given a non suspend def and 2 for old count""".stripMargin) {
    case (given Context, tree) =>
      assertEquals(
        DefDefTransforms.countContinuationSynthetics(tree.asInstanceOf[DefDef], 2),
        2)
  }

