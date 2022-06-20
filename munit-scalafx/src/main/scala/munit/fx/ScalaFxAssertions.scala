package munit
package fx

import _root_.fx.*
import org.junit.AssumptionViolatedException

/**
 * Wraps munit assertions in an Errors effect for integration within scala-fx test bodies.
 */
trait ScalaFxAssertions:
  self: Assertions =>

  /**
   * Lifts munit.Assertions#assert into Errors for testing. Will not return until given an
   * Error[AssertionError], typically within run or structured.
   *
   * @param cond
   *   The assertion value
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertFX(
      cond: => Boolean,
      clue: => Any = "assertion failed"): (Location, Errors[AssertionError]) ?=> Unit =
    liftToFX(assert(cond, clue))

  /**
   * Lifts munit.Assertions#assertEquals into Errors for testing. Will not return until given an
   * Error[AssertionError], typically within run or structured.
   *
   * @tparam A
   *   the type of the obtained value
   * @tparam B
   *   the type of the expected value
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertEqualsFX[A, B](
      obtained: A,
      expected: B,
      clue: => Any = "values are not the same"
  ): (Location, B <:< A, Errors[AssertionError]) ?=> Unit =
    liftToFX(assertEquals(obtained, expected, clue))

  /**
   * Lifts munit.Assertions#assume into Errors for testing. Will not return until given an
   * Error[AssumptionViolatedException], typically within run or structured.
   *
   * @param cond
   *   The assertion value
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assumeFX(cond: => Boolean, clue: => Any = "assumption failed")
      : (Location, Errors[AssumptionViolatedException]) ?=> Unit =
    try assume(cond, clue)
    catch case ex: AssumptionViolatedException => ex.raise

  /**
   * Lifts munit.Assertions#assertNoDiff into Errors for testing. Will not return until given an
   * Error[AssertionError], typically within run or structured.
   *
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertNoDiffFX(
      obtained: String,
      expected: String,
      clue: => Any = "diff assertion failed"): (Location, Errors[AssertionError]) ?=> Unit =
    liftToFX(assertNoDiff(obtained, expected, clue))

  /**
   * Lifts munit.Assertions#assertNotEquals into Errors for testing. Will not return until given
   * an Error[AssertionError], typically within run or structured.
   *
   * @tparam A
   *   the type of the obtained value
   * @tparam B
   *   the type of the expected value
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertNotEqualsFX[A, B](
      obtained: A,
      expected: B,
      clue: => Any = "values are the same"
  ): (Location, A =:= B, Errors[AssertionError]) ?=> Unit =
    liftToFX(assertNotEquals(obtained, expected, clue))

  /**
   * Lifts munit.Assertions#assertEqualsDoubleFX into Errors for testing. Will not return until
   * given an Error[AssertionError], typically within run or structured.
   *
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param delta
   *   Acceptable error tolerance between the expected and obtained values
   * @param clue
   * @see
   *   [munit.Clue]
   */
  def assertEqualsDoubleFX(
      obtained: Double,
      expected: Double,
      delta: Double,
      clue: => Any = "values are not the same"
  ): (Location, Errors[AssertionError]) ?=> Unit = liftToFX(
    assertEqualsDouble(obtained, expected, delta, clue))

  /**
   * Lifts munit.Assertions#assertEquals into Errors for testing. Will not return until given an
   * Error[AssertionError], typically within run or structured.
   *
   * @param obtained
   *   The actual value under test
   * @param expected
   * @param delta
   *   Acceptable error tolerance between the expected and obtained values
   * @param clue
   * @see
   *   munit.Clue
   */
  def assertEqualsFloatFX(
      obtained: Float,
      expected: Float,
      delta: Float,
      clue: => Any = "values are not the same"
  ): (Location, Errors[AssertionError]) ?=> Unit = liftToFX(
    assertEqualsFloat(obtained, expected, delta, clue))

  def assertsShiftsToFX[R, T, A](
      obtained: Errors[R] ?=> A,
      expected: T): (Location, Errors[R], Errors[AssertionError]) ?=> Unit =
    obtained
      .toEither
      .fold(
        r => assertEqualsFX(r, expected),
        a => FailException(s"expected $expected got $a", summon[Location]))

  private def liftToFX[A](
      body: Asserts[AssertionError] ?=> A): Errors[AssertionError] ?=> Unit =
    handleAssert(body)(_.raise)
