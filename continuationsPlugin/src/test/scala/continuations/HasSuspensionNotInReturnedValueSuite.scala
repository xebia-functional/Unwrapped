package continuations

import dotty.tools.dotc.ast.Trees.{Block, DefDef, Inlined}
import dotty.tools.dotc.core.Contexts.Context
import munit.FunSuite

class HasSuspensionNotInReturnedValueSuite extends FunSuite, CompilerFixtures {

  continuationsContextAndZeroAritySuspendSuspendingNotInLastRowDefDef.test(
    "should return Some(tree) when the tree has a continuation but not in the last row") {
    case (given Context, tree) =>
      assertEquals(HasSuspensionNotInReturnedValue(tree), true)
  }

  continuationsContextAndZeroAritySuspendSuspendingDefDef.test(
    "should return None when the tree has a continuation in the last row") {
    case (given Context, tree) =>
      assertEquals(HasSuspensionNotInReturnedValue(tree), false)
  }

  continuationsContextAndZeroAritySuspendSuspendingNotInLastRowIfDefDef.test(
    "should return Some(tree) when the tree has a continuation but not in the last row for structures like `if`") {
    case (given Context, tree) =>
      assertEquals(HasSuspensionNotInReturnedValue(tree), true)
  }

  continuationsContextAndZeroAritySuspendSuspendingInLastRowIfDefDef.test(
    "should return None when the tree has a continuation in the last row for structures like `if`") {
    case (given Context, tree) =>
      assertEquals(HasSuspensionNotInReturnedValue(tree), false)
  }

  continuationsContextAndZeroArityContextFunctionWithSuspensionNotInLastRowDefDef.test(
    "should return Some(tree) when the tree has a continuation but not in the last row for context functions") {
    case (given Context, tree) =>
      assertEquals(HasSuspensionNotInReturnedValue(tree), true)
  }

  continuationsContextAndZeroAritySuspendSuspendingNotInLastRowValDef.test(
    "should return Some(tree) when the tree has a continuation but not in the last row for a val") {
    case (given Context, tree) =>
      assertEquals(HasSuspensionNotInReturnedValue(tree), true)
  }

  continuationsContextAndZeroAritySuspendSuspendingValDef.test(
    "should return None when the tree has a continuation in the last row for a val") {
    case (given Context, tree) =>
      assertEquals(HasSuspensionNotInReturnedValue(tree), false)
  }
}
