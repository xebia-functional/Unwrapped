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
  def assertFX[R](cond: => Errors[R] ?=> Boolean, clue: => Any = "assertion failed")
      : Location ?=> (Errors[AssertionError], Errors[R]) ?=> Unit =
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
  def assertEqualsFX[R, A, B](
      obtained: Errors[R] ?=> A,
      expected: B,
      clue: => Any = "values are not the same"
  ): Location ?=> B <:< A ?=> (Errors[AssertionError], Errors[R]) ?=> Unit =
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
  def assumeFX[R](cond: => Errors[R] ?=> Boolean, clue: => Any = "assumption failed")
      : Location ?=> (Errors[AssumptionViolatedException], Errors[R]) ?=> Unit =
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
  def assertNoDiffFX[R](
      obtained: Errors[R] ?=> String,
      expected: String,
      clue: => Any = "diff assertion failed")
      : Location ?=> (Errors[AssertionError], Errors[R]) ?=> Unit =
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
  def assertNotEqualsFX[R, A, B](
      obtained: Errors[R] ?=> A,
      expected: B,
      clue: => Any = "values are the same"
  ): Location ?=> A =:= B ?=> (Errors[AssertionError], Errors[R]) ?=> Unit =
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
  def assertEqualsDoubleFX[R](
      obtained: Errors[R] ?=> Double,
      expected: Double,
      delta: Double,
      clue: => Any = "values are not the same"
  ): Location ?=> (Errors[AssertionError], Errors[R]) ?=> Unit = liftToFX(
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
  def assertEqualsFloatFX[R](
      obtained: Errors[R] ?=> Float,
      expected: Float,
      delta: Float,
      clue: => Any = "values are not the same"
  ): Location ?=> (Errors[AssertionError], Errors[R]) ?=> Unit = liftToFX(
    assertEqualsFloat(obtained, expected, delta, clue))

  private def liftToFX[A](
      body: Asserts[AssertionError] ?=> A): Errors[AssertionError] ?=> Unit =
    handleAssert(body)(_.raise)
