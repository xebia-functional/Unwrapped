package continuations
package types

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Types.Type
import munit.Location
import munit.internal.console.StackTraces
import munit.internal.difflib.ComparisonFailExceptionHandler
import munit.internal.difflib.Diffs

/**
 * Contains assertions for type equality
 */
trait TypeAssertions:
  self: munit.Assertions =>

  /**
   * Asserts that two types are equal using `=:=` equality.
   *
   * The "expected" value (second argument) must be the same type as the "obtained" value (first
   * argument). For example:
   * {{{
   *   assertEquals(IntType, IntType) // OK
   *   assertEquals(StringType, requiredClass(someClassFullName).typeRef) // Fails with a diff of the types' show arguments
   * }}}
   * Note that the diff does not normalize any type param info names (for now), so only the
   * actual types and positions matter, as they do in `=:=`. E.g. in
   * {{{
   *   ==> X continuations.types.ExampleTypeMapSuite.it should remove using Suspend from a method's type and insert a continuation of the final result type as a new type list at the index prior to the Suspend type.  0.719s munit.ComparisonFailException: /home/jackcviers/Documents/development/TBD/continuationsPlugin/src/test/scala/continuations/types/ExampleTypeMapSuite.scala:61
   *   60:        ctx.definitions.IntType)
   *   61:      assertTypeEquals(actualType, expectedType)
   *   62:  }
   *   values are not the same
   *   => Obtained
   *   (x$0: Int, x$1: Int)(x$0: Int): Int
   *   => Diff (- obtained, + expected)
   *   -(x$0: Int, x$1: Int)(x$0: Int): Int
   *   +(a: Int, b: Int)(completion: continuations.Continuation[Int])(c: Int): Int
   * }}}
   * the `x$0`, `x$1`, and second `x$0` in obtained are equivalent to `a`, `b`, and `c` in
   * actual, because they are in the correct position with the correct type. However,
   * `completion: continuations.Continuation[Int]` is missing in obtained, making the types
   * non-equivalent. The diff is intended to assist you in fixing the non-equivalency.
   */
  def assertTypeEquals[A <: Type, B <: Type](
      obtained: A,
      expected: B,
      clue: => Any = "values are not the same"
  )(using Location, Context): Unit = {
    def munitComparisonHandler(
        actualObtained: Any,
        actualExpected: Any
    ): ComparisonFailExceptionHandler =
      new ComparisonFailExceptionHandler {
        override def handle(
            message: String,
            unusedObtained: String,
            unusedExpected: String,
            loc: Location
        ): Nothing = failComparison(message, actualObtained, actualExpected)(loc)
      }
    StackTraces.dropInside {
      try {
        if (!(obtained =:= expected)) {
          Diffs.assertNoDiff(
            munitPrint(obtained.show),
            munitPrint(expected.show),
            munitComparisonHandler(obtained.show, expected.show),
            munitPrint(clue),
            printObtainedAsStripMargin = false
          )
        }
      } catch {
        case e: AssertionError if e.getMessage().contains("NoDenotation.owner") =>
          Diffs.assertNoDiff(
            munitPrint(obtained.show),
            munitPrint(expected.show),
            munitComparisonHandler(obtained.show, expected.show),
            munitPrint(clue),
            printObtainedAsStripMargin = false
          )
      }
    }
  }

  /**
   * Asserts that two types are not equal using `=:=` equality.
   *
   * The "expected" value (second argument) must not be the same type as the "obtained" value
   * (first argument). For example:
   * {{{
   *   assertEquals(IntType, IntType) // Fails showing the types of obtained and expected were the same type.
   *   assertEquals(StringType, requiredClass(someClassFullName).typeRef) // Ok
   * }}}
   * Note that the show does not normalize any type param info names (for now), so only the
   * actual types and positions matter, as they do in `=:=`. That is two method types of the
   * same arity with the types in the same positions are equal.
   */
  def assertNotTypeEquals[A <: Type, B <: Type](
      obtained: A,
      expected: B
  )(using Location, Context): Unit = {
    StackTraces.dropInside {
      if (!(obtained =:= expected)) {
        failComparison(
          s"expected different types: ${expected.show} =:= ${obtained.show}",
          obtained,
          expected
        )
      }
    }
  }

end TypeAssertions
